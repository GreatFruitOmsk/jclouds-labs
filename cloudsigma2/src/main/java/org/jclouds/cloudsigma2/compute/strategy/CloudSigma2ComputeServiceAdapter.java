/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.cloudsigma2.compute.strategy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.getUnchecked;
import static org.jclouds.cloudsigma2.config.CloudSigma2Properties.PROPERTY_VNC_PASSWORD;
import static org.jclouds.cloudsigma2.config.CloudSigma2Properties.TIMEOUT_DRIVE_CLONED;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.Constants;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.DeviceEmulationType;
import org.jclouds.cloudsigma2.domain.DriveInfo;
import org.jclouds.cloudsigma2.domain.DriveStatus;
import org.jclouds.cloudsigma2.domain.FirewallAction;
import org.jclouds.cloudsigma2.domain.FirewallPolicy;
import org.jclouds.cloudsigma2.domain.FirewallRule;
import org.jclouds.cloudsigma2.domain.LibraryDrive;
import org.jclouds.cloudsigma2.domain.MediaType;
import org.jclouds.cloudsigma2.domain.NIC;
import org.jclouds.cloudsigma2.domain.ServerInfo;
import org.jclouds.cloudsigma2.domain.VLANInfo;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.HardwareBuilder;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.Processor;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.domain.Location;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

@Singleton
public class CloudSigma2ComputeServiceAdapter implements
      ComputeServiceAdapter<ServerInfo, Hardware, LibraryDrive, Location> {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;
   
   private final CloudSigma2Api api;
   private final ListeningExecutorService userExecutor;
   private final String defaultVncPassword;
   private final Predicate<DriveInfo> driveCloned;

   @Inject
   public CloudSigma2ComputeServiceAdapter(CloudSigma2Api api,
         @Named(Constants.PROPERTY_USER_THREADS) ListeningExecutorService userExecutor,
         @Named(PROPERTY_VNC_PASSWORD) String defaultVncPassword,
         @Named(TIMEOUT_DRIVE_CLONED) Predicate<DriveInfo> driveCloned) {
      this.api = checkNotNull(api, "api");
      this.userExecutor = checkNotNull(userExecutor, "userExecutor");
      this.defaultVncPassword = checkNotNull(defaultVncPassword, "defaultVncPassword");
      this.driveCloned = checkNotNull(driveCloned, "driveCloned");
   }

   @Override
   public NodeAndInitialCredentials<ServerInfo> createNodeWithGroupEncodedIntoName(String tag, String name,
         Template template) {
      TemplateOptions options = template.getOptions();
      Image image = template.getImage();
      Hardware hardware = template.getHardware();

      logger.debug(">> cloning library drive %s...", image.getProviderId());
      
      LibraryDrive drive = api.cloneLibraryDrive(image.getProviderId(), new LibraryDrive.Builder().build());
      
      // TODO: We need to wait until the clone operation has completed.
      // Can we safely do this by polling the returned LibraryDrive? Is the UUID field properly populated?
      // If not, we'll have to make an additional call here to get the full populated object we've jsut cloned and
      // poll for its status
      driveCloned.apply(drive);
      checkState(drive.getStatus() == DriveStatus.UNMOUNTED, "Resource is in invalid status: %s", drive.getStatus());
      
      logger.debug(">> drive cloned (%s)...", drive);

      ImmutableList.Builder<FirewallRule> firewallRulesBuilder = ImmutableList.builder();
      for (int port : options.getInboundPorts()) {
         firewallRulesBuilder.add(new FirewallRule.Builder()
               .action(FirewallAction.ACCEPT)
               .sourcePort("" + port)
               .build());
      }

      FirewallPolicy firewallPolicy = api.createFirewallPolicy(new FirewallPolicy.Builder()
            .rules(firewallRulesBuilder.build())
            .build());
      
      // TODO: Attach the server to the networks specified by the user.
      // If the user does not explicitly provide the network identifiers,
      // should we allocate a public ip address automatically?
      ImmutableList.Builder<NIC> nics = ImmutableList.builder();
      for (String network : options.getNetworks()) {
         VLANInfo vlan = api.getVLANInfo(network);
         checkArgument(vlan != null, "network %s not found", network);
         nics.add(vlan.toNIC(firewallPolicy));
      }

      String vncPassword = Optional.fromNullable(options.getLoginPassword()).or(defaultVncPassword);
      
      ServerInfo serverInfo = api.createServer(new ServerInfo.Builder()
            .name(name)
            .cpu((int) hardware.getProcessors().get(0).getSpeed())
            .memory(BigInteger.valueOf(hardware.getRam()).multiply(BigInteger.valueOf(1024 * 1024)))
            .drives(ImmutableList.of(drive.toServerDrive(1, "0:1", DeviceEmulationType.VIRTIO)))
            .nics(nics.build())
            .meta(options.getUserMetadata())
            .tags(ImmutableList.copyOf(options.getTags()))
            .vncPassword(vncPassword)
            .build());
      api.startServer(serverInfo.getUuid());

      /*
       * TODO: The loginCredentials to be returned here are the ones that jclouds (and the final users) can use to
       * access the node, commonly via SSH in Unix systems.
       * jclouds will use these credentials to access the node to run the scripts (if configured) and bootstrap the
       * node, so we should make sure those are the good ones.
       * 
       * To accomplish this, there are several things to take into account:
       *  - If the TemplateOptions object has some "overrideLogin" option set, use those values to create the server,
       *    if the api supports it.
       *  - If the TemplateOptions contain the "authorizePublicKey" option, return the username, and if available, the private key.
       *  - Otherwise return the default SSH username and password for that server.
       */
      return new NodeAndInitialCredentials<ServerInfo>(serverInfo, serverInfo.getUuid(), LoginCredentials.builder()
            .password(serverInfo.getVncPassword())
            .authenticateSudo(true)
            .build());
   }

   @Override
   public Iterable<Hardware> listHardwareProfiles() {
      // TODO: Return a hardcoded list of hardware profiles until
      // https://issues.apache.org/jira/browse/JCLOUDS-482 is fixed
      Builder<Hardware> hardware = ImmutableSet.builder();
      Builder<Integer> ramSetBuilder = ImmutableSet.builder();
      Builder<Double> cpuSetBuilder = ImmutableSet.builder();
      for (int i = 1; i < 65; i++) {
         ramSetBuilder.add(i * 1024);
      }
      for (int i = 1; i < 41; i++) {
         cpuSetBuilder.add((double) i * 1000);
      }
      for (int ram : ramSetBuilder.build()) {
         for (double cpu : cpuSetBuilder.build()) {
            hardware.add(new HardwareBuilder()
                  .processor(new Processor(1, cpu))
                  .ram(ram)
                  .build());
         }
      }
      return hardware.build();
   }

   @Override
   public Iterable<LibraryDrive> listImages() {
      // TODO: jclouds should work with preinstalled images?
      return api.listLibraryDrives().concat().filter(new Predicate<LibraryDrive>() {
         @Override
         public boolean apply(LibraryDrive input) {
            return input.getMedia() == MediaType.DISK;
         }
      });
   }

   @Override
   public LibraryDrive getImage(String uuid) {
      return api.getLibraryDrive(uuid);
   }

   @Override
   public Iterable<Location> listLocations() {
      // Nothing to return here. Each provider will configure the locations
      return ImmutableSet.<Location> of();
   }

   @Override
   public ServerInfo getNode(String uuid) {
      return api.getServerInfo(uuid);
   }

   @Override
   public void destroyNode(String uuid) {
      api.deleteServer(uuid);
   }

   @Override
   public void rebootNode(String uuid) {
      api.stopServer(uuid);
      api.startServer(uuid);
   }

   @Override
   public void resumeNode(String uuid) {
      api.startServer(uuid);
   }

   @Override
   public void suspendNode(String uuid) {
      api.stopServer(uuid);
   }

   @Override
   public Iterable<ServerInfo> listNodes() {
      return api.listServersInfo().concat();
   }

   @Override
   public Iterable<ServerInfo> listNodesByIds(final Iterable<String> uuids) {
      // Only fetch the requested nodes. Do it in parallel.
      ListenableFuture<List<ServerInfo>> futures = allAsList(transform(uuids,
            new Function<String, ListenableFuture<ServerInfo>>() {
               @Override
               public ListenableFuture<ServerInfo> apply(final String input) {
                  return userExecutor.submit(new Callable<ServerInfo>() {
                     @Override
                     public ServerInfo call() throws Exception {
                        return api.getServerInfo(input);
                     }
                  });
               }
            }));

      return getUnchecked(futures);
   }
}

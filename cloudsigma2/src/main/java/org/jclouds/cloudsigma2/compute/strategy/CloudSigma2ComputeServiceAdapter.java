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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.getUnchecked;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import org.jclouds.Constants;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.*;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.*;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.Location;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.jclouds.domain.LoginCredentials;

@Singleton
public class CloudSigma2ComputeServiceAdapter implements
      ComputeServiceAdapter<ServerInfo, Hardware, LibraryDrive, Location> {

   private final CloudSigma2Api api;
   private final ListeningExecutorService userExecutor;

   @Inject
   public CloudSigma2ComputeServiceAdapter(CloudSigma2Api api,
         @Named(Constants.PROPERTY_USER_THREADS) ListeningExecutorService userExecutor) {
      this.api = checkNotNull(api, "api");
      this.userExecutor = checkNotNull(userExecutor, "userExecutor");
   }

   @Override
   public NodeAndInitialCredentials<ServerInfo> createNodeWithGroupEncodedIntoName(String tag, String name,
         Template template) {
      TemplateOptions options = template.getOptions();
      Image image = template.getImage();
      Hardware hardware = template.getHardware();

      LibraryDrive drive = api.cloneLibraryDrive(image.getProviderId(), new LibraryDrive.Builder().build());

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

      ServerInfo serverInfo = api.createServer(new ServerInfo.Builder()
            .name(name)
            .cpu((int) hardware.getProcessors().get(0).getSpeed())
            .memory(BigInteger.valueOf(hardware.getRam()).multiply(BigInteger.valueOf(1024 * 1024)))
            .drives(ImmutableList.of(drive.toServerDrive(1, "", DeviceEmulationType.VIRTIO)))
            .nics(ImmutableList.of(firewallPolicy.toNIC()))
            .meta(options.getUserMetadata())
            .tags(ImmutableList.copyOf(options.getTags()))
            .vncPassword(options.getLoginPassword())
            .build());
      api.startServer(serverInfo.getUuid());

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
      return api.listLibraryDrives().concat();
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

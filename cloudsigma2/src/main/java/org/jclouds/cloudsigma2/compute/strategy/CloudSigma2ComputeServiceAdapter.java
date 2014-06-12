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

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.Constants;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.LibraryDrive;
import org.jclouds.cloudsigma2.domain.ServerInfo;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.Location;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

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
   public NodeAndInitialCredentials<ServerInfo> createNodeWithGroupEncodedIntoName(String s, String s2,
         Template template) {
      
      /*
       * Create the node
       *  - template.getImage() contains the Image as transformed by the LibraryDriveToImage function.
       *    this means the template.getImage().getProviderId() returns the UUID of the library drive
       *    that should be used to create the node.
       *  - template.getHardware() will contain the CPU and RAM information that must be used to create
       *    the node. It may also contain the Disk information, if it is relevant to the provider.
       *  - template.getOptions() will contain a CloudSigma2TemplateOptions instance with provider specific
       *    options that have been set by the user, and the jclouds standard options (such as login keys, inbound
       *    ports, etc). The options must be taken into account to create additional resources (keypairs, open
       *    ports, etc) that are required to create the server according to the provided spec.
       */
      
      return null;
   }

   @Override
   public Iterable<Hardware> listHardwareProfiles() {
      // TODO: Return a hardcoded list of hardware profiles until
      // https://issues.apache.org/jira/browse/JCLOUDS-482 is fixed
      return null;
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

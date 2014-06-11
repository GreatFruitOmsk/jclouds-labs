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
package org.jclouds.cloudsigma2.compute;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.LibraryDrive;
import org.jclouds.cloudsigma2.domain.ServerInfo;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Template;
import org.jclouds.domain.Location;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.filter;

@Singleton
public class CloudSigma2ComputeServiceAdapter implements ComputeServiceAdapter<ServerInfo, Hardware, LibraryDrive, Location> {
   private final CloudSigma2Api client;

   @Inject
   public CloudSigma2ComputeServiceAdapter(CloudSigma2Api client) {
      this.client = client;
   }

   @Override
   public NodeAndInitialCredentials<ServerInfo> createNodeWithGroupEncodedIntoName(String s, String s2, Template template) {
      return null;
   }

   @Override
   public Iterable<Hardware> listHardwareProfiles() {
      return null;
   }

   @Override
   public Iterable<LibraryDrive> listImages() {
      return client.listLibraryDrives().concat();
   }

   @Override
   public LibraryDrive getImage(String uuid) {
      return client.getLibraryDrive(uuid);
   }

   @Override
   public Iterable<Location> listLocations() {
      return ImmutableSet.<Location>of();
   }

   @Override
   public ServerInfo getNode(String uuid) {
      return client.getServerInfo(uuid);
   }

   @Override
   public void destroyNode(String uuid) {
      client.deleteServer(uuid);
   }

   @Override
   public void rebootNode(String uuid) {
      client.stopServer(uuid);
      client.startServer(uuid);
   }

   @Override
   public void resumeNode(String uuid) {
      client.startServer(uuid);
   }

   @Override
   public void suspendNode(String uuid) {
      client.stopServer(uuid);
   }

   @Override
   public Iterable<ServerInfo> listNodes() {
      return client.listServersInfo().concat();
   }

   @Override
   public Iterable<ServerInfo> listNodesByIds(final Iterable<String> uuids) {
      return filter(listNodes(), new Predicate<ServerInfo>() {
         @Override
         public boolean apply(ServerInfo serverInfo) {
            return contains(uuids, serverInfo.getUuid());
         }
      });
   }
}

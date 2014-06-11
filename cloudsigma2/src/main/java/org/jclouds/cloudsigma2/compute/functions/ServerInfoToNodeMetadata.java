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
package org.jclouds.cloudsigma2.compute.functions;

import static com.google.common.collect.Iterables.filter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.*;
import org.jclouds.compute.domain.*;
import org.jclouds.compute.domain.NodeMetadata.Status;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

public class ServerInfoToNodeMetadata implements Function<ServerInfo, NodeMetadata> {
   public static final Map<ServerStatus, NodeMetadata.Status> serverStatusToNodeStatus = ImmutableMap
         .<ServerStatus, NodeMetadata.Status>builder().put(ServerStatus.RUNNING, Status.RUNNING)
         .put(ServerStatus.STARTING, Status.PENDING)
         .put(ServerStatus.STOPPING, Status.PENDING)
         .put(ServerStatus.STOPPED, Status.SUSPENDED)
         .put(ServerStatus.PAUSED, Status.SUSPENDED)
         .put(ServerStatus.UNAVAILABLE, Status.SUSPENDED)
         .put(ServerStatus.UNRECOGNIZED, Status.UNRECOGNIZED)
         .build();

   private final ServerDriveToVolume serverDriveToVolume;
   private final NICToAddress nicToAddress;

   @Inject
   public ServerInfoToNodeMetadata(ServerDriveToVolume serverDriveToVolume, NICToAddress nicToAddress) {
      this.serverDriveToVolume = serverDriveToVolume;
      this.nicToAddress = nicToAddress;
   }

   @Override
   public NodeMetadata apply(ServerInfo serverInfo) {
      NodeMetadataBuilder builder = new NodeMetadataBuilder();

      builder.id(serverInfo.getUuid());
      builder.name(serverInfo.getName());

      builder.hardware(new HardwareBuilder()
            .id(serverInfo.getUuid())
            .processor(new Processor(1, serverInfo.getCpu()))
            .ram(serverInfo.getMemory().intValue())
            .volumes(Iterables.transform(serverInfo.getDrives(), serverDriveToVolume))
            .build());
      builder.status(serverStatusToNodeStatus.get(serverInfo.getStatus()));
      builder.publicAddresses(filter(Iterables.transform(serverInfo.getNics(), nicToAddress),
            new Predicate<String>() {
               @Override
               public boolean apply(String address) {
                  return address != null;
               }
            }));
      return builder.build();
   }

   @Singleton
   public static final class ServerDriveToVolume implements Function<ServerDrive, Volume> {

      private final CloudSigma2Api client;

      @Inject
      public ServerDriveToVolume(CloudSigma2Api client) {
         this.client = client;
      }

      @Override
      public Volume apply(ServerDrive serverDrive) {
         VolumeBuilder builder = new VolumeBuilder();
         DriveInfo driveInfo = client.getDriveInfo(serverDrive.getDriveUuid());
         builder.id(driveInfo.getUuid());
         builder.size(driveInfo.getSize().floatValue());
         builder.durable(true);
         builder.type(Volume.Type.NAS);
         return builder.build();
      }
   }

   @Singleton
   public static final class NICToAddress implements Function<NIC, String> {

      @Override
      public String apply(NIC nic) {
         IPConfiguration ipV4Configuration = nic.getIpV4Configuration();
         IPConfiguration ipV6Configuration = nic.getIpV4Configuration();
         if (ipV4Configuration != null) {
            return ipV4Configuration.getIp().getUuid();
         } else if (ipV6Configuration != null) {
            return ipV6Configuration.getIp().getUuid();
         }
         return null;
      }
   }
}

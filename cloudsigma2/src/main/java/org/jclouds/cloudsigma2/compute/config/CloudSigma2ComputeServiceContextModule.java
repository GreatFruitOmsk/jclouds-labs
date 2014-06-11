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
package org.jclouds.cloudsigma2.compute.config;

import com.google.common.base.Function;
import com.google.inject.TypeLiteral;
import org.jclouds.cloudsigma2.compute.CloudSigma2ComputeServiceAdapter;
import org.jclouds.cloudsigma2.compute.functions.LibraryDriveToImage;
import org.jclouds.cloudsigma2.compute.functions.ServerInfoToNodeMetadata;
import org.jclouds.cloudsigma2.compute.functions.ServerInfoToNodeMetadata.NICToAddress;
import org.jclouds.cloudsigma2.compute.functions.ServerInfoToNodeMetadata.ServerDriveToVolume;
import org.jclouds.cloudsigma2.domain.LibraryDrive;
import org.jclouds.cloudsigma2.domain.NIC;
import org.jclouds.cloudsigma2.domain.ServerDrive;
import org.jclouds.cloudsigma2.domain.ServerInfo;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.config.ComputeServiceAdapterContextModule;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Volume;
import org.jclouds.domain.Location;

public class CloudSigma2ComputeServiceContextModule extends
      ComputeServiceAdapterContextModule<ServerInfo, Hardware, LibraryDrive, Location> {

   @Override
   protected void configure() {
      super.configure();
      bind(new TypeLiteral<ComputeServiceAdapter<ServerInfo, Hardware, LibraryDrive, Location>>() {
      }).to(CloudSigma2ComputeServiceAdapter.class);
      bind(new TypeLiteral<Function<ServerInfo, NodeMetadata>>() {
      }).to(ServerInfoToNodeMetadata.class);
      bind(new TypeLiteral<Function<LibraryDrive, Image>>() {
      }).to(LibraryDriveToImage.class);
      bind(new TypeLiteral<Function<ServerDrive, Volume>>() {
      }).to(ServerDriveToVolume.class);
      bind(new TypeLiteral<Function<NIC, String>>() {
      }).to(NICToAddress.class);
   }
}

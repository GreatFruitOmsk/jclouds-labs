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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.cloudsigma2.domain.DriveStatus;
import org.jclouds.cloudsigma2.domain.LibraryDrive;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.ImageBuilder;
import org.jclouds.compute.domain.OperatingSystem;

import com.google.common.base.Function;
import org.jclouds.compute.domain.OsFamily;

@Singleton
public class LibraryDriveToImage implements Function<LibraryDrive, Image> {

   private final Map<DriveStatus, Image.Status> driveStatusToNodeStatus;

   @Inject
   public LibraryDriveToImage(Map<DriveStatus, Image.Status> driveStatusToNodeStatus) {
      this.driveStatusToNodeStatus = checkNotNull(driveStatusToNodeStatus, "driveStatusToNodeStatus");
   }

   @Override
   public Image apply(LibraryDrive libraryDrive) {
      return new ImageBuilder()
         .id(libraryDrive.getUuid())
         .userMetadata(libraryDrive.getMeta())
         .name(libraryDrive.getName())
         .description(libraryDrive.getDescription())
         .operatingSystem(OperatingSystem.builder()
               .name(libraryDrive.getName())
               .arch(libraryDrive.getArch())
               .family(OsFamily.fromValue(libraryDrive.getOs()))
               .version(libraryDrive.getVersion())
               .description(libraryDrive.getDescription())
               .build())
         .status(driveStatusToNodeStatus.get(libraryDrive.getStatus()))
         .build();
   }
}

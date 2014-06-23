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

import com.google.inject.Guice;
import org.jclouds.cloudsigma2.domain.*;
import org.jclouds.compute.domain.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class ServerDriveToVolumeTest {

   private static final ServerDriveToVolume SERVER_DRIVE_TO_VOLUME = Guice
         .createInjector()
         .getInstance(ServerDriveToVolume.class);

   private ServerDrive input;
   private Volume expected;

   @BeforeMethod
   public void setUp() throws Exception {
      input = new ServerDrive.Builder()
            .bootOrder(1)
            .deviceChannel("0:1")
            .deviceEmulationType(DeviceEmulationType.VIRTIO)
            .drive(new Drive.Builder()
                  .uuid("f17cce62-bcc9-4e0b-a57b-a5582b05aff0")
                  .build())
            .build();

      expected = new VolumeBuilder()
            .id("f17cce62-bcc9-4e0b-a57b-a5582b05aff0")
            .size(1024000000.f)
            .durable(true)
            .type(Volume.Type.NAS)
            .bootDevice(true)
            .build();
   }

   public void test() {
      Assert.assertEquals(SERVER_DRIVE_TO_VOLUME.apply(input), expected);
   }
}

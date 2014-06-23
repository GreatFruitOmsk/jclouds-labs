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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import org.jclouds.cloudsigma2.domain.*;
import org.jclouds.compute.domain.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.net.URI;

@Test
public class ServerInfoToNodeMetadataTest {
   private static final ServerInfoToNodeMetadata SERVER_INFO_TO_NODE_METADATA = Guice
         .createInjector()
         .getInstance(ServerInfoToNodeMetadata.class);

   private ServerInfo input;
   private NodeMetadata expected;

   @BeforeMethod
   public void setUp() throws Exception {
      input = new ServerInfo.Builder()
            .uuid("a19a425f-9e92-42f6-89fb-6361203071bb")
            .name("test_acc_full_server")
            .cpu(1000)
            .memory(new BigInteger("268435456"))
            .status(ServerStatus.STOPPED)
            .drives(ImmutableList.of(
                  new Drive.Builder()
                        .uuid("ae78e68c-9daa-4471-8878-0bb87fa80260")
                        .resourceUri(new URI("/api/2.0/drives/ae78e68c-9daa-4471-8878-0bb87fa80260/"))
                        .build().toServerDrive(0, "0:0", DeviceEmulationType.IDE),
                  new Drive.Builder()
                        .uuid("22826af4-d6c8-4d39-bd41-9cea86df2976")
                        .resourceUri(new URI("/api/2.0/drives/22826af4-d6c8-4d39-bd41-9cea86df2976/"))
                        .build().toServerDrive(1, "0:0", DeviceEmulationType.VIRTIO)))
            .nics(ImmutableList.of(new NIC.Builder()
                  .ipV4Configuration(new IPConfiguration.Builder()
                        .ip(new IP.Builder().uuid("1.2.3.4").build())
                        .build())
                  .build()))
            .build();

      expected = new NodeMetadataBuilder()
            .id("a19a425f-9e92-42f6-89fb-6361203071bb")
            .name("test_acc_full_server")
            .status(NodeMetadata.Status.SUSPENDED)
            .hardware(new HardwareBuilder()
                  .id("a19a425f-9e92-42f6-89fb-6361203071bb")
                  .processor(new Processor(1, 1000))
                  .ram(268435456)
                  .volumes(ImmutableSet.of(
                        new VolumeBuilder()
                              .id("ae78e68c-9daa-4471-8878-0bb87fa80260")
                              .size(1024000f)
                              .durable(true)
                              .type(Volume.Type.NAS)
                              .bootDevice(false)
                              .build(),
                        new VolumeBuilder()
                              .id("22826af4-d6c8-4d39-bd41-9cea86df2976")
                              .size(1024000f)
                              .durable(true)
                              .type(Volume.Type.NAS)
                              .bootDevice(true)
                              .build()))
                  .build())
            .publicAddresses(ImmutableSet.of("1.2.3.4"))
            .build();
   }

   public void test() {
      Assert.assertEquals(SERVER_INFO_TO_NODE_METADATA.apply(input), expected);
   }
}

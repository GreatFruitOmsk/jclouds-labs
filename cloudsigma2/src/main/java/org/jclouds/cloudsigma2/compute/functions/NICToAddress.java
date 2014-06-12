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

import javax.inject.Singleton;

import org.jclouds.cloudsigma2.domain.IPConfiguration;
import org.jclouds.cloudsigma2.domain.NIC;

import com.google.common.base.Function;

@Singleton
public final class NICToAddress implements Function<NIC, String> {

   @Override
   public String apply(NIC nic) {
      IPConfiguration ipV4Configuration = nic.getIpV4Configuration();
      IPConfiguration ipV6Configuration = nic.getIpV6Configuration();
      // TODO: Is the UUID the address (such as 1.2.3.4) or it is just an UUID?
      // The jclouds model requires the address, not the UUID.
      if (ipV4Configuration != null) {
         return ipV4Configuration.getIp().getUuid();
      } else if (ipV6Configuration != null) {
         return ipV6Configuration.getIp().getUuid();
      }
      return null;
   }
}
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jclouds.cloudsigma2.domain.FirewallAction;
import org.jclouds.cloudsigma2.domain.FirewallIpProtocol;
import org.jclouds.cloudsigma2.domain.FirewallRule;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class FirewallRuleToIpPermissions implements Function<FirewallRule, Iterable<IpPermission>> {

   private final Map<FirewallIpProtocol, IpProtocol> firewallIpProtocolToIpProtocol;

   @Inject
   public FirewallRuleToIpPermissions(Map<FirewallIpProtocol, IpProtocol> firewallIpProtocolToIpProtocol) {
      this.firewallIpProtocolToIpProtocol = checkNotNull(firewallIpProtocolToIpProtocol,
            "firewallIpProtocolToIpProtocol");
   }

   @Override
   public Iterable<IpPermission> apply(FirewallRule input) {
      List<IpPermission> ipPermissions = Lists.newArrayList();
      List<int []> portRangeSet = Lists.newArrayList();
      String destinationPort = input.getDestinationPort();
      if (input.getAction().equals(FirewallAction.ACCEPT)) {
         if (destinationPort != null) {
            if (destinationPort.contains("!")) {
               int startPort;
               int endPort;
               destinationPort = destinationPort.substring(destinationPort.indexOf("!") + 1,
                     destinationPort.length());
               if (destinationPort.contains(":")) {
                  int[] ports = parsePort(destinationPort);
                  startPort = ports[0];
                  endPort = ports[1];
               } else {
                  startPort = Integer.parseInt(destinationPort);
                  endPort = startPort;
               }

               if (startPort > 0) {
                  portRangeSet.add(new int[]{0, startPort - 1});
               }
               if (endPort < 65535) {
                  portRangeSet.add(new int[]{endPort + 1, 65535});
               }
            } else {
               if (destinationPort.contains(":")) {
                  int[] ports = parsePort(destinationPort);
                  portRangeSet.add(new int[]{ports[0], ports[1]});
               } else {
                  int port = Integer.parseInt(destinationPort);
                  portRangeSet.add(new int[]{port, port});
               }
            }
         } else {
            portRangeSet.add(new int[]{0, 65535});
         }
      } else if (input.getAction().equals(FirewallAction.DROP)) {
         if (destinationPort != null) {
            if (destinationPort.contains("!")) {
               destinationPort = destinationPort.substring(destinationPort.indexOf("!") + 1,
                     destinationPort.length());
               if (destinationPort.contains(":")) {
                  int[] ports = parsePort(destinationPort);
                  portRangeSet.add(new int[]{ports[0], ports[1]});
               } else {
                  int port = Integer.parseInt(destinationPort);
                  portRangeSet.add(new int[]{port, port});
               }
            } else {
               int startPort;
               int endPort;
               if (destinationPort.contains(":")) {
                  int[] ports = parsePort(destinationPort);
                  startPort = ports[0];
                  endPort = ports[1];
               } else {
                  startPort = Integer.parseInt(destinationPort);
                  endPort = startPort;
               }

               if (startPort > 0) {
                  portRangeSet.add(new int[]{0, startPort - 1});
               }
               if (endPort < 65535) {
                  portRangeSet.add(new int[]{endPort + 1, 65535});
               }
            }
         }
      }

      for (int[] portRange : portRangeSet) {
         IpPermission.Builder permissionBuilder = IpPermission.builder();
         permissionBuilder.fromPort(portRange[0]);
         permissionBuilder.toPort(portRange[1]);
         permissionBuilder.ipProtocol(input.getIpProtocol() != null ? firewallIpProtocolToIpProtocol.get(input
               .getIpProtocol()) : IpProtocol.UNRECOGNIZED);
         if (input.getSourceIp() != null) {
            permissionBuilder.cidrBlock(input.getSourceIp());
         } else {
            permissionBuilder.cidrBlock("0.0.0.0/0");
         }
         ipPermissions.add(permissionBuilder.build());
      }
      return ipPermissions;
   }

   private int[] parsePort(String portRange) {
      int[] ports = new int[2];
      String[] portStringsArray = portRange.split(":");
      ports[0] = Integer.parseInt(portStringsArray[0]);
      ports[1] = Integer.parseInt(portStringsArray[1]);
      return ports;
   }
}

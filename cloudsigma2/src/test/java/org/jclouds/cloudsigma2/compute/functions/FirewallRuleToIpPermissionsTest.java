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
import org.jclouds.cloudsigma2.domain.FirewallAction;
import org.jclouds.cloudsigma2.domain.FirewallDirection;
import org.jclouds.cloudsigma2.domain.FirewallIpProtocol;
import org.jclouds.cloudsigma2.domain.FirewallRule;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.jclouds.cloudsigma2.compute.config.CloudSigma2ComputeServiceContextModule.firewallIpProtocolToIpProtocol;
import static org.testng.Assert.assertEquals;

@Test(groups = "unit", testName = "FirewallPolicyToSecurityGroupTest")
public class FirewallRuleToIpPermissionsTest {

   private List<FirewallRule> input;
   private ImmutableList<ImmutableList<IpPermission>> expected;

   @BeforeMethod
   public void setUp() throws Exception {
      input = ImmutableList.of(
            new FirewallRule.Builder()
                  .action(FirewallAction.ACCEPT)
                  .comment("Allow SSH traffic to the VM from our office in Dubai")
                  .direction(FirewallDirection.IN)
                  .destinationPort("22")
                  .ipProtocol(FirewallIpProtocol.TCP)
                  .sourceIp("172.66.32.0/24")
                  .build(),
            new FirewallRule.Builder()
                  .action(FirewallAction.ACCEPT)
                  .comment("Drop SSH traffic to the VM from our office in Dubai")
                  .direction(FirewallDirection.IN)
                  .destinationPort("!22")
                  .ipProtocol(FirewallIpProtocol.TCP)
                  .sourceIp("172.66.32.0/24")
                  .build(),
            new FirewallRule.Builder()
                  .action(FirewallAction.ACCEPT)
                  .comment("Allow traffic from 10-233 ports to the VM")
                  .direction(FirewallDirection.IN)
                  .destinationPort("10:233")
                  .ipProtocol(FirewallIpProtocol.UDP)
                  .build(),
            new FirewallRule.Builder()
                  .action(FirewallAction.ACCEPT)
                  .comment("Drop traffic from 10-233 ports to the VM")
                  .direction(FirewallDirection.IN)
                  .destinationPort("!10:233")
                  .ipProtocol(FirewallIpProtocol.UDP)
                  .build(),
            new FirewallRule.Builder()
                  .action(FirewallAction.DROP)
                  .comment("Drop HTTP traffic to the VM from our office in Dubai")
                  .direction(FirewallDirection.IN)
                  .destinationPort("80")
                  .ipProtocol(FirewallIpProtocol.TCP)
                  .sourceIp("172.66.32.0/24")
                  .build(),
            new FirewallRule.Builder()
                  .action(FirewallAction.DROP)
                  .comment("Accept HTTP traffic to the VM from our office in Dubai")
                  .direction(FirewallDirection.IN)
                  .destinationPort("!80")
                  .ipProtocol(FirewallIpProtocol.TCP)
                  .sourceIp("172.66.32.0/24")
                  .build(),
            new FirewallRule.Builder()
                  .action(FirewallAction.DROP)
                  .direction(FirewallDirection.IN)
                  .destinationPort("100:300")
                  .ipProtocol(FirewallIpProtocol.UDP)
                  .build(),
            new FirewallRule.Builder()
                  .action(FirewallAction.DROP)
                  .direction(FirewallDirection.IN)
                  .destinationPort("!100:300")
                  .ipProtocol(FirewallIpProtocol.UDP)
                  .build());
      expected = ImmutableList.of(
            ImmutableList.of(new IpPermission.Builder()
                  .ipProtocol(IpProtocol.TCP)
                  .cidrBlock("172.66.32.0/24")
                  .fromPort(22)
                  .toPort(22)
                  .build()),
            ImmutableList.of(new IpPermission.Builder()
                        .ipProtocol(IpProtocol.TCP)
                        .cidrBlock("172.66.32.0/24")
                        .fromPort(0)
                        .toPort(21)
                        .build(),
                  new IpPermission.Builder()
                        .ipProtocol(IpProtocol.TCP)
                        .cidrBlock("172.66.32.0/24")
                        .fromPort(23)
                        .toPort(65535)
                        .build()),
            ImmutableList.of(new IpPermission.Builder()
                  .ipProtocol(IpProtocol.UDP)
                  .cidrBlock("0.0.0.0/0")
                  .fromPort(10)
                  .toPort(233)
                  .build()),
            ImmutableList.of(new IpPermission.Builder()
                        .ipProtocol(IpProtocol.UDP)
                        .cidrBlock("0.0.0.0/0")
                        .fromPort(0)
                        .toPort(9)
                        .build(),
                  new IpPermission.Builder()
                        .ipProtocol(IpProtocol.UDP)
                        .cidrBlock("0.0.0.0/0")
                        .fromPort(234)
                        .toPort(65535)
                        .build()),
            ImmutableList.of(new IpPermission.Builder()
                        .ipProtocol(IpProtocol.TCP)
                        .fromPort(0)
                        .toPort(79)
                        .cidrBlock("172.66.32.0/24")
                        .build(),
                  new IpPermission.Builder()
                        .ipProtocol(IpProtocol.TCP)
                        .fromPort(81)
                        .toPort(65535)
                        .cidrBlock("172.66.32.0/24")
                        .build()),
            ImmutableList.of(new IpPermission.Builder()
                  .ipProtocol(IpProtocol.TCP)
                  .fromPort(80)
                  .toPort(80)
                  .cidrBlock("172.66.32.0/24")
                  .build()),
            ImmutableList.of(new IpPermission.Builder()
                        .ipProtocol(IpProtocol.UDP)
                        .fromPort(0)
                        .toPort(99)
                        .cidrBlock("0.0.0.0/0")
                        .build(),
                  new IpPermission.Builder()
                        .ipProtocol(IpProtocol.UDP)
                        .fromPort(301)
                        .toPort(65535)
                        .cidrBlock("0.0.0.0/0")
                        .build()),
            ImmutableList.of(new IpPermission.Builder()
                  .ipProtocol(IpProtocol.UDP)
                  .fromPort(100)
                  .toPort(300)
                  .cidrBlock("0.0.0.0/0")
                  .build()));
   }

   public void testConvertFirewallRules() {
      FirewallRuleToIpPermissions function = new FirewallRuleToIpPermissions(firewallIpProtocolToIpProtocol);
      for (int i = 0; i < input.size() - 1; i++) {
         assertEquals(function.apply(input.get(i)), expected.get(i));
      }
   }
}

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
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;
import org.easymock.EasyMock;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.CloudSigma2ApiMetadata;
import org.jclouds.cloudsigma2.domain.FirewallAction;
import org.jclouds.cloudsigma2.domain.FirewallDirection;
import org.jclouds.cloudsigma2.domain.FirewallIpProtocol;
import org.jclouds.cloudsigma2.domain.FirewallPolicy;
import org.jclouds.cloudsigma2.domain.FirewallRule;
import org.jclouds.compute.domain.SecurityGroup;
import org.jclouds.compute.domain.SecurityGroupBuilder;
import org.jclouds.compute.functions.GroupNamingConvention;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static org.jclouds.cloudsigma2.compute.config.CloudSigma2ComputeServiceContextModule.firewallIpProtocolToIpProtocol;
import static org.testng.Assert.assertEquals;

@Test(groups = "unit", testName = "FirewallPolicyToSecurityGroupTest")
public class FirewallPolicyToSecurityGroupTest {

   private FirewallPolicy input;
   private SecurityGroup expected;
   private GroupNamingConvention.Factory namingConvention;

   @BeforeMethod
   public void setUp() throws Exception {
      input = new FirewallPolicy.Builder()
            .name("My awesome policy")
            .uuid("cf8479b4-c98b-46c8-ab9c-108bb00c8218")
            .resourceUri(new URI("/api/2.0/fwpolicies/cf8479b4-c98b-46c8-ab9c-108bb00c8218/"))
            .meta(ImmutableMap.of("description", "test firewall policy",
                  "test key", "test value"))
            .rules(ImmutableList.of(
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
                        .build()))
            .build();

      expected = new SecurityGroupBuilder()
            .name("My awesome policy")
            .ids("cf8479b4-c98b-46c8-ab9c-108bb00c8218")
            .uri(new URI("/api/2.0/fwpolicies/cf8479b4-c98b-46c8-ab9c-108bb00c8218/"))
            .userMetadata(ImmutableMap.of("description", "test firewall policy",
                  "test key", "test value"))
            .ipPermissions(ImmutableList.of(
                  new IpPermission.Builder()
                        .ipProtocol(IpProtocol.TCP)
                        .cidrBlock("172.66.32.0/24")
                        .fromPort(22)
                        .toPort(22)
                        .build(),
                  new IpPermission.Builder()
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
                        .build(),
                  new IpPermission.Builder()
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
                        .build(),
                  new IpPermission.Builder()
                        .ipProtocol(IpProtocol.TCP)
                        .fromPort(80)
                        .toPort(80)
                        .cidrBlock("172.66.32.0/24")
                        .build()))
            .build();

      namingConvention = Guice.createInjector(new AbstractModule() {
         @Override
         protected void configure() {
            Names.bindProperties(binder(), new CloudSigma2ApiMetadata().getDefaultProperties());
         }
      }).getInstance(GroupNamingConvention.Factory.class);
   }

   public void testConvertFirewallPolicy() {
      CloudSigma2Api api = EasyMock.createMock(CloudSigma2Api.class);
      FirewallPolicyToSecurityGroup function = new FirewallPolicyToSecurityGroup(api, namingConvention,
            new FirewallRuleToIpPermissions(firewallIpProtocolToIpProtocol));
      SecurityGroup converted = function.apply(input);
      assertEquals(converted, expected);
   }

}

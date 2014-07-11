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
package org.jclouds.cloudsigma2.compute.extensions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.util.$ImmutableList;
import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.compute.functions.FirewallPolicyToSecurityGroup;
import org.jclouds.cloudsigma2.domain.FirewallIpProtocol;
import org.jclouds.cloudsigma2.domain.FirewallPolicy;
import org.jclouds.cloudsigma2.domain.FirewallRule;
import org.jclouds.cloudsigma2.domain.NIC;
import org.jclouds.cloudsigma2.domain.ServerInfo;
import org.jclouds.compute.domain.SecurityGroup;
import org.jclouds.compute.extensions.SecurityGroupExtension;
import org.jclouds.domain.Location;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;

import com.google.common.collect.Multimap;

import javax.inject.Inject;

public class CloudSigma2SecurityGroupExtension implements SecurityGroupExtension {

   private final CloudSigma2Api api;
   private final FirewallPolicyToSecurityGroup firewallPolicyToSecurityGroup;
   private final Map<IpProtocol, FirewallIpProtocol> ipProtocolToFirewallIpProtocol;

   @Inject
   public CloudSigma2SecurityGroupExtension(CloudSigma2Api api,
         FirewallPolicyToSecurityGroup firewallPolicyToSecurityGroup,
         Map<IpProtocol, FirewallIpProtocol> ipProtocolToFirewallIpProtocol) {
      this.ipProtocolToFirewallIpProtocol =
            checkNotNull(ipProtocolToFirewallIpProtocol, "ipProtocolToFirewallIpProtocol");
      this.api = checkNotNull(api, "api");
      this.firewallPolicyToSecurityGroup = checkNotNull(firewallPolicyToSecurityGroup, "firewallPolicyToSecurityGroup");
   }

   @Override
   public Set<SecurityGroup> listSecurityGroups() {
      return ImmutableSet.copyOf(transform(api.listFirewallPoliciesInfo().concat(), firewallPolicyToSecurityGroup));
   }

   @Override
   public Set<SecurityGroup> listSecurityGroupsInLocation(Location location) {
      return this.listSecurityGroups();
   }

   @Override
   public Set<SecurityGroup> listSecurityGroupsForNode(String id) {
      ServerInfo serverInfo = api.getServerInfo(id);

      Iterable<NIC> nicsWithPolicies = filter(serverInfo.getNics(), new Predicate<NIC>() {
         @Override
         public boolean apply(NIC input) {
            return input.getFirewallPolicy() != null;
         }
      });

      return ImmutableSet.copyOf(transform(nicsWithPolicies, new Function<NIC, SecurityGroup>() {
         @Override
         public SecurityGroup apply(NIC input) {
            return firewallPolicyToSecurityGroup.apply(api.getFirewallPolicy(input.getFirewallPolicy().getUuid()));
         }
      }));
   }

   @Override
   public SecurityGroup getSecurityGroupById(String id) {
      return firewallPolicyToSecurityGroup.apply(api.getFirewallPolicy(id));
   }

   @Override
   public SecurityGroup createSecurityGroup(String name, Location location) {
      FirewallPolicy firewallPolicy = api.createFirewallPolicy(new FirewallPolicy.Builder().name(name).build());
      return firewallPolicyToSecurityGroup.apply(firewallPolicy);
   }

   @Override
   public boolean removeSecurityGroup(String id) {
      //TODO Only policies attached to servers in status stopped can be deleted.
      api.deleteFirewallPolicy(id);
      return false;
   }

   @Override
   public SecurityGroup addIpPermission(IpPermission ipPermission, SecurityGroup group) {
      return this.addIpPermission(ipPermission.getIpProtocol(), ipPermission.getFromPort(), ipPermission.getFromPort(),
            null, null, null, group);
   }

   @Override
   public SecurityGroup removeIpPermission(IpPermission ipPermission, SecurityGroup group) {
      return this.removeIpPermission(ipPermission.getIpProtocol(), ipPermission.getFromPort(), ipPermission.getToPort(),
            null, null, null, group);
   }

   @Override
   public SecurityGroup addIpPermission(IpProtocol protocol, int startPort, int endPort,
         Multimap<String, String> tenantIdGroupNamePairs, Iterable<String> ipRanges, Iterable<String> groupIds,
         SecurityGroup group) {
      FirewallPolicy firewallPolicy = api.getFirewallPolicy(group.getId());
      List<FirewallRule> firewallRules = firewallPolicy.getRules();
      firewallRules.add(new FirewallRule.Builder()
            .ipProtocol(ipProtocolToFirewallIpProtocol.get(protocol))
            .destinationPort("" + endPort)
            .sourcePort("" + startPort)
            .build());
      firewallPolicy = api.editFirewallPolicy(firewallPolicy.getUuid(), FirewallPolicy.Builder
            .fromFirewallPolicy(firewallPolicy)
            .rules(firewallRules)
            .build());
      return firewallPolicyToSecurityGroup.apply(firewallPolicy);
   }

   @Override
   public SecurityGroup removeIpPermission(final IpProtocol protocol, final int startPort, final int endPort,
         Multimap<String, String> tenantIdGroupNamePairs, Iterable<String> ipRanges, Iterable<String> groupIds,
         SecurityGroup group) {
      FirewallPolicy firewallPolicy = api.getFirewallPolicy(group.getId());
      firewallPolicy = api.editFirewallPolicy(firewallPolicy.getUuid(), FirewallPolicy.Builder
            .fromFirewallPolicy(firewallPolicy)
            .rules($ImmutableList.copyOf(filter(firewallPolicy.getRules(), new Predicate<FirewallRule>() {
               @Override
               public boolean apply(FirewallRule input) {
                  return !input.getIpProtocol().equals(ipProtocolToFirewallIpProtocol.get(protocol)) &&
                        !input.getDestinationPort().equals("" + endPort) &&
                        !input.getSourcePort().equals("" + startPort);
               }
            })))
            .build());
      return firewallPolicyToSecurityGroup.apply(firewallPolicy);
   }

   @Override
   public boolean supportsTenantIdGroupNamePairs() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean supportsTenantIdGroupIdPairs() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean supportsGroupIds() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean supportsPortRangesForGroups() {
      // TODO Auto-generated method stub
      return false;
   }

   
}

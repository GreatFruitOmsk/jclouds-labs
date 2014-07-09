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

import java.util.Set;

import org.jclouds.compute.domain.SecurityGroup;
import org.jclouds.compute.extensions.SecurityGroupExtension;
import org.jclouds.domain.Location;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;

import com.google.common.collect.Multimap;

public class CloudSigma2SecurityGroupExtension implements SecurityGroupExtension {

   @Override
   public Set<SecurityGroup> listSecurityGroups() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<SecurityGroup> listSecurityGroupsInLocation(Location location) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<SecurityGroup> listSecurityGroupsForNode(String id) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SecurityGroup getSecurityGroupById(String id) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SecurityGroup createSecurityGroup(String name, Location location) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean removeSecurityGroup(String id) {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public SecurityGroup addIpPermission(IpPermission ipPermission, SecurityGroup group) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SecurityGroup removeIpPermission(IpPermission ipPermission, SecurityGroup group) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SecurityGroup addIpPermission(IpProtocol protocol, int startPort, int endPort,
         Multimap<String, String> tenantIdGroupNamePairs, Iterable<String> ipRanges, Iterable<String> groupIds,
         SecurityGroup group) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SecurityGroup removeIpPermission(IpProtocol protocol, int startPort, int endPort,
         Multimap<String, String> tenantIdGroupNamePairs, Iterable<String> ipRanges, Iterable<String> groupIds,
         SecurityGroup group) {
      // TODO Auto-generated method stub
      return null;
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

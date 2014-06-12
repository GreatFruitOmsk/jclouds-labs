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
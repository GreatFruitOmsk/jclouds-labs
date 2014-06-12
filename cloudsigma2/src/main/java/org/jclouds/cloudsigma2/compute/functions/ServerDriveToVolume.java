package org.jclouds.cloudsigma2.compute.functions;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.cloudsigma2.CloudSigma2Api;
import org.jclouds.cloudsigma2.domain.DriveInfo;
import org.jclouds.cloudsigma2.domain.ServerDrive;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.domain.VolumeBuilder;

import com.google.common.base.Function;

@Singleton
public final class ServerDriveToVolume implements Function<ServerDrive, Volume> {

   private final CloudSigma2Api api;

   @Inject
   public ServerDriveToVolume(CloudSigma2Api api) {
      this.api = checkNotNull(api, "api");
   }

   @Override
   public Volume apply(ServerDrive serverDrive) {
      VolumeBuilder builder = new VolumeBuilder();
      DriveInfo driveInfo = api.getDriveInfo(serverDrive.getDriveUuid());
      builder.id(driveInfo.getUuid());
      builder.size(driveInfo.getSize().floatValue());
      builder.durable(true);
      builder.type(Volume.Type.NAS);
      // TODO: Do we know if the volume is the boot device?
      return builder.build();
   }
}
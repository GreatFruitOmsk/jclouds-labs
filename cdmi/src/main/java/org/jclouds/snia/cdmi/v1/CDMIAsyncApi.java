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
package org.jclouds.snia.cdmi.v1;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jclouds.rest.annotations.Delegate;
import org.jclouds.snia.cdmi.v1.features.ContainerAsyncApi;
import org.jclouds.snia.cdmi.v1.features.DataAsyncApi;
import org.jclouds.snia.cdmi.v1.features.DataNonCDMIContentTypeAsyncApi;
import org.jclouds.snia.cdmi.v1.features.DomainAsyncApi;

/**
 * Provides asynchronous access to CDMI via their REST API.
 * <p/>
 * 
 * @see CDMIApi
 * @see <a href="http://www.snia.org/cdmi">api doc</a>
 */
public interface CDMIAsyncApi {

   /**
    * Provides asynchronous access to Domain Object Resource Operations.
    */
   @Delegate
   DomainAsyncApi getDomainApi();

   /**
    * Provides asynchronous access to Container Object Resource Operations.
    */
   @Delegate
   ContainerAsyncApi getApi();

   /**
    * Provides asynchronous access to Data Object Resource Operations.
    */
   @Delegate
   @Path("/{containerName}")
   DataAsyncApi getDataApiForContainer(@PathParam("containerName") String containerName);

   /**
    * Provides asynchronous access to Data Object Resource Operations.
    */
   @Delegate
   @Path("/{containerName}")
   DataNonCDMIContentTypeAsyncApi getDataNonCDMIContentTypeApiForContainer(
            @PathParam("containerName") String containerName);

}

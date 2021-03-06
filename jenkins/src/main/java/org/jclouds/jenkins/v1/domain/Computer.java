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
package org.jclouds.jenkins.v1.domain;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * @see <a
 *      href="http://ci.jruby.org/computer/api/">api
 *      doc</a>
 */
public class Computer implements Comparable<Computer> {

   public static Builder builder() {
      return new Builder();
   }

   public Builder toBuilder() {
      return builder().fromComputerMetadata(this);
   }

   public static class Builder {
      protected String displayName;
      protected boolean idle;
      protected boolean offline;

      /**
       * @see Computer#getDisplayName()
       */
      public Builder displayName(String displayName) {
         this.displayName = checkNotNull(displayName, "displayName");
         return this;
      }

      /**
       * @see Computer#isIdle()
       */
      public Builder idle(boolean idle) {
         this.idle = idle;
         return this;
      }

      /**
       * @see Computer#isOffline()
       */
      public Builder offline(boolean offline) {
         this.offline = offline;
         return this;
      }

      public Computer build() {
         return new Computer(displayName, idle, offline);
      }

      public Builder fromComputerMetadata(Computer from) {
         return displayName(from.getDisplayName()).idle(from.isIdle()).offline(from.isOffline());
      }
   }

   protected final String displayName;
   protected final boolean idle;
   protected final boolean offline;

   public Computer(String displayName, boolean idle, boolean offline) {
      this.displayName = checkNotNull(displayName, "displayName");
      this.idle = idle;
      this.offline = offline;
   }

   /**
    * 
    * @return the displayName of the computer
    */
   public String getDisplayName() {
      return displayName;
   }

   /**
    * 
    * @return the number of objects in the computer
    */
   public boolean isIdle() {
      return idle;
   }

   /**
    * @return the total offline stored in this computer
    */
   public boolean isOffline() {
      return offline;
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      }
      if (object instanceof Computer) {
         final Computer other = Computer.class.cast(object);
         return equal(getDisplayName(), other.getDisplayName()) && equal(isIdle(), other.isIdle())
                  && equal(isOffline(), other.isOffline());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(getDisplayName(), isIdle(), isOffline());
   }

   @Override
   public String toString() {
      return string().toString();
   }

   protected ToStringHelper string() {
      return toStringHelper("").add("displayName", getDisplayName()).add("idle", isIdle()).add("offline", isOffline());
   }

   @Override
   public int compareTo(Computer that) {
      if (that == null)
         return 1;
      if (this == that)
         return 0;
      return this.getDisplayName().compareTo(that.getDisplayName());
   }

}

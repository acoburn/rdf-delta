/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seaborne.delta.server.system;

import org.apache.jena.system.JenaSubsystemLifecycle ;
import org.apache.jena.system.JenaSystem ;

/** If you want to trigger Delta init for jena ...
 *  name this is META-INF/services/org.apache.jena.system.JenaSubsystemLifecycle
 *  (not normally done this way - can also happen in InitJenaDeltaServerLocal_
 */
public class JenaInitHook implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        boolean original = DeltaSystem.DEBUG_INIT;
        DeltaSystem.DEBUG_INIT = DeltaSystem.DEBUG_INIT | JenaSystem.DEBUG_INIT;
        //DeltaSystem.init();
        DeltaSystem.DEBUG_INIT = original;
    }

    @Override
    public void stop() {}
    
    @Override
    public int level() {
        // Jena level
        return 9000;
    }
}
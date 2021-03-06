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

package org.seaborne.delta.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.tdb.base.file.Location;
import org.junit.Before;
import org.junit.Test;
import org.seaborne.delta.DeltaException;
import org.seaborne.delta.Id;
import org.seaborne.delta.lib.IOX;
import org.seaborne.delta.server.local.LocalServer;
import org.seaborne.delta.server.local.patchlog.PatchStore ;

/**
 * Tests of {@link LocalServer} for creating and 
 * deleteing a {@link LocalServer} area.
 * 
 * See {@link TestLocalServer} for tests involving
 * a static setup of data sources.
 */

public class TestLocalServerCreateDelete {

    // Testing area that is created and modified by tests. 
    private static String DIR = "target/testing/delta";

    private static void initialize() {
        PatchStore.clearPatchLogs();
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        // copy in setup.
        try { FileUtils.copyDirectory(new File(TestLocalServer.SERVER_DIR), new File(DIR)); }
        catch (IOException ex) { throw IOX.exception(ex); }
    }
    
    @Before public void beforeTest() {
        initialize();
    }
    
    @Test public void local_server_create_01() {
        Location loc = Location.create(DIR);
        LocalServer server = LocalServer.attach(loc);
    }
    
    @Test public void local_server_create_02() {
        Location loc = Location.create(DIR);
        LocalServer server1 = LocalServer.attach(loc);
        LocalServer server2 = LocalServer.attach(loc);
        assertEquals(server1, server2);
    }

    @Test public void datasource_create_01() {
        Location loc = Location.create(DIR);
        LocalServer server = LocalServer.attach(loc);
        Id newId = server.createDataSource(false, "XYZ", "http://example/xyz");
        assertNotNull(newId);
    }
    
    // Create does not overwrite
    @Test public void datasource_create_02() {
        Location loc = Location.create(DIR);
        LocalServer server = LocalServer.attach(loc);
        
        Id newId1 = server.createDataSource(false, "XYZ", "http://example/xyz");
        try {
            Id newId2 = server.createDataSource(false, "XYZ", "http://example/xyz");
            fail("Expected createDataSource to fail");
        } catch (DeltaException ex) {}
    }
    
    // Create does not overwrite persistent state.
    @Test public void local_server_create_03() {
        Location loc = Location.create(DIR);
        LocalServer server1 = LocalServer.attach(loc);
        Id newId1 = server1.createDataSource(false, "XYZ", "http://example/xyz");
        LocalServer.release(server1);

        LocalServer server2 = LocalServer.attach(loc);
        try {
            Id newId2 = server2.createDataSource(false, "XYZ", "http://example/xyz");
            fail("Expected createDataSource to fail");
        } catch (DeltaException ex) {}
    }
    
    // "Restart" test.
    @Test public void local_server_restart_01() {
        Location loc = Location.create(DIR);
        LocalServer server1 = LocalServer.attach(loc);
        Id newId1 = server1.createDataSource(false, "XYZ", "http://example/xyz");
        LocalServer.release(server1);
        
        LocalServer server2 = LocalServer.attach(loc);
        // 3 - data1, data2 and the new XYZ.
        assertEquals(3, server2.listDataSources().size());
        assertEquals(3, server2.listDataSourcesIds().size());
        
        long z = server2.listDataSourcesIds().stream().filter(id->id.equals(newId1)).count();
        assertEquals("Count of newId occurences", 1, z);
        
        Id id = server2.listDataSourcesIds().stream().filter(_id->_id.equals(newId1)).findFirst().get();
        assertEquals(newId1, id) ;

        List<Id> ids = server2.listDataSources().stream().map(dss->dss.getId()).collect(Collectors.toList());
        assertTrue(ids.contains(newId1));
    }

}

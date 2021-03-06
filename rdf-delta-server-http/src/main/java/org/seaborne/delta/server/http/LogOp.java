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

package org.seaborne.delta.server.http;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.UUID;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonNumber ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.web.HttpSC ;
import org.seaborne.delta.*;
import org.seaborne.patch.RDFPatch ;
import org.seaborne.patch.RDFPatchOps ;
import org.slf4j.Logger ;

public class LogOp {
    static private Logger LOG = Delta.getDeltaLogger("Patch") ;
    
    /** Execute an append, assuming the action has been verified that it is an appened operation */ 
    public static void append(DeltaAction action) throws IOException {
        LOG.info("Patch:append");
        Id dsRef = idForDatasource(action);
        if ( dsRef == null )
            throw new DeltaNotFoundException("No such datasource: '"+action.httpArgs.datasourceName+"'");
        
        try (InputStream in = action.request.getInputStream()) {
            RDFPatch patch = RDFPatchOps.read(in);

            if ( false )
                RDFPatchOps.write(System.out, patch);

            long version = action.dLink.append(dsRef, patch);
            // Location of patch in "container/patch/id" form.
            //String location = action.request.getRequestURI()+"/patch/"+ref.asPlainString();
            String location = action.request.getRequestURI()+"?version="+version;

            JsonValue x = JsonNumber.value(version);
            JsonValue rslt = JsonBuilder.create()
                .startObject()
                .pair(DeltaConst.F_VERSION, version)
                .pair(DeltaConst.F_LOCATION, location)
                .finishObject()
                .build();
            OutputStream out = action.response.getOutputStream();
            action.response.setContentType(WebContent.contentTypeJSON);
            action.response.setStatus(HttpSC.OK_200);
            action.response.setHeader(HttpNames.hLocation, location);
            JSON.write(out, rslt);
            out.flush();
        }
    }
    
    private static Id idForDatasource(DeltaAction action) {
        String datasourceName = action.httpArgs.datasourceName;
        if ( Id.maybeUUID(datasourceName) ) {
            // Looks like an Id
            try { 
                UUID uuid = UUID.fromString(datasourceName);
                Id id = Id.fromUUID(uuid);
                DataSourceDescription dsd = action.dLink.getDataSourceDescription(id);
                return dsd != null ? id : null; 
            } catch (IllegalArgumentException ex) { /* Not a UUID: drop through to try-by-name */ }
        }
        // Not a UUID.
        DataSourceDescription dsd = action.dLink.getDataSourceDescriptionByName(datasourceName);
        return dsd != null ? dsd.getId() : null;
    }

    public static void fetch(DeltaAction action) throws IOException {
        LOG.info("Patch:fetch");
        Id dsRef = idForDatasource(action);
        if ( dsRef == null )
            throw new DeltaNotFoundException("No such datasource: '"+action.httpArgs.datasourceName+"'");
        RDFPatch patch;
        
        if ( action.httpArgs.patchId != null ) {
            Id patchId = action.httpArgs.patchId;
            patch = action.dLink.fetch(dsRef, patchId);
            if ( patch == null )
                throw new DeltaNotFoundException("Patch not found: id="+patchId);
        } else if ( action.httpArgs.version != null ) {
            patch = action.dLink.fetch(dsRef, action.httpArgs.version);
            if ( patch == null )
                throw new DeltaNotFoundException("Patch not found: version="+action.httpArgs.version);
        } else {
            DeltaAction.errorBadRequest("No id and no version in patch fetch request");
            patch = null;
        }
        
        OutputStream out = action.response.getOutputStream();
        //action.response.setCharacterEncoding(WebContent.charsetUTF8);
        action.response.setStatus(HttpSC.OK_200);
        action.response.setContentType(DeltaConst.contentTypePatchText); 
        RDFPatchOps.write(out, patch);
        // Not "close".
        IO.flush(out);
    }
}

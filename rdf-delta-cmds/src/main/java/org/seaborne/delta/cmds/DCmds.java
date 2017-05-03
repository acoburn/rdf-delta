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

package org.seaborne.delta.cmds;

import java.util.List ;
import java.util.Objects ;
import java.util.Optional ;

import jena.cmd.CmdException ;
import org.apache.jena.atlas.json.JsonException ;
import org.apache.jena.atlas.web.HttpException ;
import org.seaborne.delta.DataSourceDescription ;
import org.seaborne.delta.Id ;
import org.seaborne.delta.link.DeltaLink ;

class DCmds {
    static void ping(DeltaLink dLink) {
        Objects.requireNonNull(dLink);
        try {
            dLink.ping();
        } catch (HttpException ex) {
            Throwable ex2 = ex;
            if ( ex.getCause() != null )
                ex2 = ex.getCause();
            
            throw new CmdException(ex2.getMessage());
        } catch (JsonException ex) {
            throw new CmdException("Not an RDF Patch server"); 
        }
    }

    static void list(DeltaLink dLink) {
        Objects.requireNonNull(dLink);
        List <DataSourceDescription> all = dLink.allDescriptions();
        if ( all.isEmpty())
            System.out.println("No logs currently");
        else    
            all.forEach(System.out::println);
    }

    static void create(DeltaLink dLink, String name, String url) {
        Objects.requireNonNull(dLink);
        Objects.requireNonNull(name);
        Objects.requireNonNull(url);
        
        List <DataSourceDescription> all = dLink.allDescriptions();
        boolean b = all.stream().anyMatch(dsd-> Objects.equals(dsd.name, name));
        if ( b )
            throw new CmdException("Source '"+name+"' already exists");
        Id id = dLink.newDataSource(name, url);
        DataSourceDescription dsd = dLink.getDataSourceDescription(id);
        System.out.println("Created "+dsd);
    }

    static void hide(DeltaLink dLink, String name) {
        Optional<Id> opt = find(dLink, name);
        if ( ! opt.isPresent() )
            throw new CmdException("Source '"+name+"' does not exist");
        Id dsRef = opt.get(); 
        dLink.removeDataset(dsRef);
    }

    static Optional<Id> find(DeltaLink dLink, String name) {
        Objects.requireNonNull(dLink);
        Objects.requireNonNull(name);
        
        List <DataSourceDescription> all = dLink.allDescriptions();
        return 
            all.stream()
               .filter(dsd-> Objects.equals(dsd.name, name))
               .findFirst()
               .map(dsd->dsd.id);
    }
}
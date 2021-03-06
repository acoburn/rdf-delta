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

package org.seaborne.riot.tio ;

import java.util.Iterator ;
import java.util.stream.Stream ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.riot.tokens.Token ;

/** Deliver {@code Tuple<Token>} */
public interface TupleReader extends Iterator<Tuple<Token>>, Iterable<Tuple<Token>>, Closeable {
    // Not AutoClosable because this typically wraps a stream provided by the caller. 
    // XXX Convert to stream only?
    public default Stream<Tuple<Token>> stream() {
        return Iter.asStream(this) ;
    }
    
    @Override
    public default void remove() {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public default Iterator<Tuple<Token>> iterator() {
        return this ;
    }

    @Override
    public default void close() {}
}

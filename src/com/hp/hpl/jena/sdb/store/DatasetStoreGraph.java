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

package com.hp.hpl.jena.sdb.store;

import java.util.Iterator ;

import org.openjena.atlas.lib.Closeable ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.graph.GraphSDB ;
import com.hp.hpl.jena.sdb.util.StoreUtils ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.shared.LockMRSW ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphCaching ;
import com.hp.hpl.jena.sparql.core.DatasetImpl ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;

public class DatasetStoreGraph extends DatasetGraphCaching
    implements DatasetGraph, Closeable, GraphStore 
{
    final Store store ;
    Graph defaultGraph = null ;
    Lock lock = new LockMRSW() ;
    final Context context ;
    
    public DatasetStoreGraph(Store store, Context context)
    {
        this(store, null, context) ;
    }
    
    public DatasetStoreGraph(Store store, GraphSDB graph, Context context)
    {
        this.store = store ;
        // Force the "default" graph
        this.defaultGraph = graph ;
        this.context = context ;
    }
    
    public Store getStore() { return store ; }
    
    @Override
    public Iterator<Node> listGraphNodes()
    {
        return StoreUtils.storeGraphNames(store) ;
    }

    //---- Update
    @Override
    public void startRequest()
    {}

    @Override
    public void finishRequest()
    {}

    @Override
    public Dataset toDataset()
    { return new DatasetImpl(this) ; }
    
    @Override
    protected boolean _containsGraph(Node graphNode)
    {
        return StoreUtils.containsGraph(store, graphNode) ;
    }

    @Override
    protected Graph _createDefaultGraph()
    {
        return new GraphSDB(store) ;
    }

    @Override
    protected Graph _createNamedGraph(Node graphNode)
    {
        return new GraphSDB(store, graphNode) ;
    }

    // Use unsubtle helper versions (the bulk loader copes with large additions).
    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    { Helper.addToDftGraph(this, s, p, o) ; }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    { Helper.addToNamedGraph(this, g, s, p, o) ; }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o)
    { Helper.deleteFromDftGraph(this, s, p, o) ; }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o)
    { Helper.deleteFromNamedGraph(this, g, s, p, o) ; }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    { return Helper.findInDftGraph(this, s, p, o) ; }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    { return Helper.findInAnyNamedGraphs(this, s, p, o) ; } 

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    { return Helper.findInSpecificNamedGraph(this, g, s, p, o) ; }

    @Override
    protected void _close()
    { store.close() ; }

}

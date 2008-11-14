/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph.assembler;

import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.exactlyOneProperty;
import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.getStringValue;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pIndex;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pLocation;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;

import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.assembler.IndexAssembler;
import com.hp.hpl.jena.tdb.base.file.Location;

/*public*/ class PGraphAssembler extends AssemblerBase implements Assembler
{
    static IndexAssembler indexAssembler = null ; 
    // See Store/gbt.ttl
    
    @Override
    public Model open(Assembler a, Resource root, Mode mode)
    {
        // In case we go via explicit index construction,
        // although given we got here, the assembler is wired in
        // and that probably means TDB.init
        TDB.init() ;
        
        // Make a model.
        // [] rdf:type tdb:GraphTDB ;
        //    tdb:location "dir" ;
        
        // Or [not tested]
        // [] rdf:type tdb:GraphTDB ;
        //      tdb:index "SPO" ;
        //    .
        
        // Or [not ready]
        // [] rdf:type tdb:GraphBDB ;
        
        if ( ! exactlyOneProperty(root, pLocation) )
            throw new AssemblerException(root, "No location given") ;

        String dir = getStringValue(root, pLocation) ;
        Location loc = new Location(dir) ;
        if ( ! root.hasProperty(pIndex) )
            // Make just using the location.
            return TDBFactory.createModel(loc) ;
        
        // ---- API ways
        
        
        String[] indexes = null ;

        StmtIterator sIter = root.listProperties(pIndex) ;
        while(sIter.hasNext())
        {
            RDFNode obj = sIter.nextStatement().getObject() ;
            if ( obj.isLiteral() )
            {
                String desc = ((Literal)obj).getString() ;
                System.out.printf("Index: %s\n", desc) ; System.out.flush();
                continue ;
            }
            throw new TDBException("Wrong format for tdb:index: should be a string: found: "+FmtUtils.stringForRDFNode(obj)) ; 
//            Resource x = (Resource)obj ;
//            String desc = x.getProperty(pDescription).getString() ;
//            String file = x.getProperty(pFile).getString() ;
//            System.out.printf("Index: %s in file %s\n", desc, file) ; System.out.flush();
        }
        
        System.out.flush();
        throw new TDBException("Custom indexes turned off") ; 
        
//        // Make and call directly.         
//        tripleIndexBuilder = new TripleIndexAssembler(loc) ;
//        // ------- Experimental : Make using explicit index descriptions
//        // ---- Uses BTree, not BPlusTrees - need upgrading. 
//        Map<String, TripleIndex> indexes = new HashMap<String, TripleIndex>() ;
//        @SuppressWarnings("unchecked")
//        List<Resource> indexDesc = (List<Resource>)multiValueResource(root, pIndex ) ;
//        if ( indexes.size() > 3 )
//            throw new AssemblerException(root, "More than 3 indexes!") ;
//        for ( Resource r : indexDesc )
//        {
//            TripleIndex idx = (TripleIndex)tripleIndexBuilder.open(a, r, mode) ;
//            String d = idx.getDescription() ;
//            if ( indexes.containsKey(d) )
//                throw new AssemblerException(root, format("Index %s declared twice", d)) ;
//            // Check one of SPO, POS, OPS.
//            if ( ! ( d.equalsIgnoreCase(Names.indexSPO) || 
//                     d.equalsIgnoreCase(Names.indexPOS) || 
//                     d.equalsIgnoreCase(Names.indexOSP) ))
//                throw new AssemblerException(root, format("Unrecognized description (expected SPO, POS or OSP)", d)) ;
//            indexes.put(idx.getDescription(), idx) ;
//        }
//        
//        NodeTable nodeTable = new NodeTableIndex(IndexBuilder.get(), loc) ;
//        
//        GraphTDB graph = new GraphTDB(indexes.get(Names.indexSPO), 
//                                      indexes.get(Names.indexPOS),               
//                                      indexes.get(Names.indexOSP),
//                                      nodeTable,
//                                      null) ;
//        return ModelFactory.createModelForGraph(graph) ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
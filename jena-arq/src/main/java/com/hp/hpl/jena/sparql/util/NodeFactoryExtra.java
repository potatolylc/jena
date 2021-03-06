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

package com.hp.hpl.jena.sparql.util;

import java.util.Calendar;

import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/**
 * Various convenience helper methods for converting to and from nodes
 *
 */
public class NodeFactoryExtra
{
    private static final PrefixMap prefixMappingDefault = PrefixMapFactory.createForInput(SSE.getDefaultPrefixMapRead()) ; 
    
    /** 
     * Parse a node - with convenience prefix mapping 
     * <p>
     * Allows surrounding white space
     * </p>
     * @param nodeString Node string to parse
     *  
     */ 
    public static Node parseNode(String nodeString)
    {
        return parseNode(nodeString, prefixMappingDefault) ;
    }

    private static PrefixMap pmapEmpty = PrefixMapFactory.create() ; 

    /** Parse a string into a node. 
     * <p>
     * Allows surrounding white space.
     * </p>
     * @param nodeString Node string to parse
     * @param pmap Prefix Map, null to use no prefix mappings
     * @return Parsed Node
     * @throws RiotException Thrown if a valid node cannot be parsed
     */ 
    public static Node parseNode(String nodeString, PrefixMap pmap)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(nodeString) ;
        if ( ! tokenizer.hasNext() )
            throw new RiotException("Empty RDF term") ; 
        Token token = tokenizer.next() ;
        Node node = token.asNode(pmap) ;
        if ( node == null )
            throw new RiotException("Bad RDF Term: "+nodeString) ;

        if ( tokenizer.hasNext() )
            throw new RiotException("Trailing characters in string: "+nodeString) ;
        if ( node.isURI() )
        {
            // Lightly test for bad URIs.
            String x = node.getURI() ;
            if ( x.indexOf(' ') >= 0 )
                throw new RiotException("Space(s) in  IRI: "+ nodeString) ;
        }
        return node ;
    }
    
    private static QueryParseException makeException(String msg, int line, int column)
    {
        return new QueryParseException(msg, line, column) ;
    }
    
    public static Node createLiteralNode(String lex, String lang, String datatypeURI)
    {
        if ( datatypeURI != null && datatypeURI.equals("") )
            datatypeURI = null ;
        
        if ( lang != null && lang.equals("") )
            lang = null ;
        
        RDFDatatype dType = null ;
        if ( datatypeURI != null )
            dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
        
        Node n = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, lang, dType) ;
        return n ;
    }
    
    public static int nodeToInt(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        
        if ( ! XSDDatatype.XSDinteger.isValidLiteral(lit) )
            return Integer.MIN_VALUE ;
        int i = ((Number)lit.getValue()).intValue() ;
        return i ;
    }
    
    public static long nodeToLong(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        
        if ( ! XSDDatatype.XSDinteger.isValidLiteral(lit) )
            return Long.MIN_VALUE ;
        long i = ((Number)lit.getValue()).longValue() ;
        return i ;
    }
    
    public static float nodeToFloat(Node node)
    {
        LiteralLabel lit = node.getLiteral();
        
        if ( ! XSDDatatype.XSDfloat.isValidLiteral(lit) )
            return Float.NaN;
        float f = ((Number)lit.getValue()).floatValue();
        return f;
    }
    
    public static double nodeToDouble(Node node)
    {
        LiteralLabel lit = node.getLiteral();
        
        if ( ! XSDDatatype.XSDdouble.isValidLiteral(lit) )
            return Double.NaN;
        double d = ((Number)lit.getValue()).doubleValue();
        return d;
    }
    
    public static Node intToNode(int integer)
    {
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(Integer.toString(integer), "", XSDDatatype.XSDinteger) ;
    }

    public static Node intToNode(long integer)
    {
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(Long.toString(integer), "", XSDDatatype.XSDinteger) ;
    }

    public static Node floatToNode(float value)
    {
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(Float.toString(value), "", XSDDatatype.XSDfloat) ;
    }
    
    public static Node doubleToNode(double value)
    {
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(Double.toString(value), "", XSDDatatype.XSDdouble) ;
    }
    
    public static Node dateTimeToNode(Calendar c)
    {
        String lex = Utils.calendarToXSDDateTimeString(c);
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdateTime);
    }
    
    public static Node dateToNode(Calendar c)
    {
        String lex = Utils.calendarToXSDDateString(c);
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdate);
    }
    
    public static Node timeToNode(Calendar c)
    {
        String lex = Utils.calendarToXSDTimeString(c);
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, null, XSDDatatype.XSDtime);
    }

    public static Node nowAsDateTime()
    {
        String lex = Utils.nowAsXSDDateTimeString() ;
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
    }

    public static Node todayAsDate()
    {
        String lex = Utils.todayAsXSDDateString() ;
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdate) ;
    }

}

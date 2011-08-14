/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import com.hp.hpl.jena.sparql.util.TranslationTable ;

public enum ResultFormat
{ 
    // 1 - enum? (this code predates Java 1.5 and enums!)
    // 2 - merge with out system wide naming (WebContent?)
    FMT_RS_XML , FMT_RS_JSON , FMT_RS_CSV , FMT_RS_TSV , FMT_RS_SSE , FMT_RS_BIO ,
    FMT_NONE , FMT_TEXT , FMT_TUPLES , FMT_COUNT ,
    FMT_RS_RDF , FMT_RDF_XML , FMT_RDF_N3 , FMT_RDF_TTL , FMT_RDF_TURTLE , FMT_RDF_NT ,
    FMT_UNKNOWN ;
    
    // Common names to symbol (used by arq.rset)
    private static TranslationTable<ResultFormat> names = new TranslationTable<ResultFormat>(true) ;
    static {
        names.put("srx",         FMT_RS_XML) ;
        names.put("xml",         FMT_RS_XML) ;
        
        names.put("json",        FMT_RS_JSON) ;
        names.put("yaml",        FMT_RS_JSON) ;    // The JSON format is a subset of YAML
        names.put("sse",         FMT_RS_SSE) ;
        names.put("csv",         FMT_RS_CSV) ;
        names.put("tsv",         FMT_RS_TSV) ;
        names.put("srb",         FMT_RS_BIO) ;
        names.put("text",        FMT_TEXT) ;
        names.put("count",       FMT_COUNT) ;
        names.put("tuples",      FMT_TUPLES) ;
        names.put("none",        FMT_NONE) ;

        
        names.put("rdf",         FMT_RDF_XML) ; 
        names.put("rdf/n3",      FMT_RDF_N3) ;
        names.put("rdf/xml",     FMT_RDF_XML) ;
        names.put("n3",          FMT_RDF_N3) ;
        names.put("ttl",         FMT_RDF_TTL) ;
        names.put("turtle",      FMT_RDF_TTL) ;
        names.put("graph",       FMT_RDF_TTL) ;
        names.put("nt",          FMT_RDF_NT) ;
        names.put("n-triples",   FMT_RDF_NT) ;

    }

    public static ResultFormat guessSyntax(String url) 
    {
        return guessSyntax(url, FMT_RS_XML) ;
    }
    
    public static boolean isRDFGraphSyntax(ResultFormat fmt)
    {
        if ( FMT_RDF_N3.equals(fmt) ) return true ;
        if ( FMT_RDF_TURTLE.equals(fmt) ) return true ;
        if ( FMT_RDF_XML.equals(fmt) ) return true ;
        if ( FMT_RDF_NT.equals(fmt) ) return true ;
        return false ;
    }
    
    public static ResultFormat guessSyntax(String url, ResultFormat defaultFormat)
    {
        // -- XML
        if ( url.endsWith(".srx") )
            return FMT_RS_XML ;
        if ( url.endsWith(".xml") )
            return FMT_RS_XML ;
        
        // -- Some kind of RDF
        if ( url.endsWith(".rdf") )
            return FMT_RDF_XML ;
        if ( url.endsWith(".n3") )
            return FMT_RDF_N3 ;
        if ( url.endsWith(".ttl") )
            return FMT_RDF_TURTLE ;
        
        // -- JSON
        if ( url.endsWith(".srj") )
            return FMT_RS_JSON ;
        if ( url.endsWith(".json") )
            return FMT_RS_JSON ;
        if ( url.endsWith(".yml") )
            return FMT_RS_JSON ;
        
        // -- SSE : http://openjena.org/wiki/SSE
        if ( url.endsWith(".sse") )
            return FMT_RS_SSE ;

        if ( url.endsWith(".srb") ) // BindingsIO format.
            return FMT_RS_BIO ;

        // Likely to be something completely different!
        if ( url.endsWith(".csv") )
            return FMT_RS_CSV ;
        if ( url.endsWith(".tsv") )
            return FMT_RS_TSV ;
        
        return defaultFormat ;
    }
    
    
    /** Look up a short name for a result set FMT_
     * 
     * @param s  Short name
     * @return  ResultSetFormat
     */
 
    public static ResultFormat lookup(String s)
    {
        return names.lookup(s) ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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
/*****************************************************************************
 * Source code information
 * -----------------------
 * Package            Jena 2
 * Web site           http://jena.sourceforge.net
 * Created            08 Jan 2004 15:26
 * Filename           $RCSfile: OWL.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     @releaseStatus@ $State: Exp $
 *
 * Last modified on   $Date: 2004-05-10 13:40:41 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/


// Package
///////////////////////////////////////
package com.hp.hpl.jena.vocabulary;


// Imports
///////////////////////////////////////
import com.hp.hpl.jena.rdf.model.*;




/**
 * Vocabulary definitions from file:vocabularies/owl.owl
 * @author Auto-generated by jena.schemagen on 08 Jan 2004 15:26
 */
public class OWL {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    protected static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://www.w3.org/2002/07/owl#";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS; }
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** A resource that denotes the OWL-full sublanguage of OWL (value &quot;{@value}&quot;) */
    public static final Resource FULL_LANG = m_model.getResource( getURI() );
    
    /** A resource, not officially sanctioned by WebOnt, that denotes the OWL-DL sublanguage of OWL (value &quot;{@value}&quot; )*/
    public static final Resource DL_LANG = m_model.getResource( "http://www.w3.org/TR/owl-features/#term_OWLDL" );
    
    /** A resource, not officially sanctioned by WebOnt, that denotes the OWL-Lite sublanguage of OWL (value &quot;{@value}&quot; )*/
    public static final Resource LITE_LANG = m_model.getResource( "http://www.w3.org/TR/owl-features/#term_OWLLite" );
    
    
    // Vocabulary properties
    ///////////////////////////

    public static final Property maxCardinality = m_model.createProperty( "http://www.w3.org/2002/07/owl#maxCardinality" );
    
    public static final Property versionInfo = m_model.createProperty( "http://www.w3.org/2002/07/owl#versionInfo" );
    
    public static final Property equivalentClass = m_model.createProperty( "http://www.w3.org/2002/07/owl#equivalentClass" );
    
    public static final Property distinctMembers = m_model.createProperty( "http://www.w3.org/2002/07/owl#distinctMembers" );
    
    public static final Property oneOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#oneOf" );
    
    public static final Property sameAs = m_model.createProperty( "http://www.w3.org/2002/07/owl#sameAs" );
    
    public static final Property incompatibleWith = m_model.createProperty( "http://www.w3.org/2002/07/owl#incompatibleWith" );
    
    public static final Property minCardinality = m_model.createProperty( "http://www.w3.org/2002/07/owl#minCardinality" );
    
    public static final Property complementOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#complementOf" );
    
    public static final Property onProperty = m_model.createProperty( "http://www.w3.org/2002/07/owl#onProperty" );
    
    public static final Property equivalentProperty = m_model.createProperty( "http://www.w3.org/2002/07/owl#equivalentProperty" );
    
    public static final Property inverseOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#inverseOf" );
    
    public static final Property backwardCompatibleWith = m_model.createProperty( "http://www.w3.org/2002/07/owl#backwardCompatibleWith" );
    
    public static final Property differentFrom = m_model.createProperty( "http://www.w3.org/2002/07/owl#differentFrom" );
    
    public static final Property priorVersion = m_model.createProperty( "http://www.w3.org/2002/07/owl#priorVersion" );
    
    public static final Property imports = m_model.createProperty( "http://www.w3.org/2002/07/owl#imports" );
    
    public static final Property allValuesFrom = m_model.createProperty( "http://www.w3.org/2002/07/owl#allValuesFrom" );
    
    public static final Property unionOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#unionOf" );
    
    public static final Property hasValue = m_model.createProperty( "http://www.w3.org/2002/07/owl#hasValue" );
    
    public static final Property someValuesFrom = m_model.createProperty( "http://www.w3.org/2002/07/owl#someValuesFrom" );
    
    public static final Property disjointWith = m_model.createProperty( "http://www.w3.org/2002/07/owl#disjointWith" );
    
    public static final Property cardinality = m_model.createProperty( "http://www.w3.org/2002/07/owl#cardinality" );
    
    public static final Property intersectionOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#intersectionOf" );
    

    // Vocabulary classes
    ///////////////////////////

    public static final Resource Thing = m_model.createResource( "http://www.w3.org/2002/07/owl#Thing" );
    
    public static final Resource DataRange = m_model.createResource( "http://www.w3.org/2002/07/owl#DataRange" );
    
    public static final Resource Ontology = m_model.createResource( "http://www.w3.org/2002/07/owl#Ontology" );
    
    public static final Resource DeprecatedClass = m_model.createResource( "http://www.w3.org/2002/07/owl#DeprecatedClass" );
    
    public static final Resource AllDifferent = m_model.createResource( "http://www.w3.org/2002/07/owl#AllDifferent" );
    
    public static final Resource DatatypeProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#DatatypeProperty" );
    
    public static final Resource SymmetricProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#SymmetricProperty" );
    
    public static final Resource TransitiveProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#TransitiveProperty" );
    
    public static final Resource DeprecatedProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#DeprecatedProperty" );
    
    public static final Resource AnnotationProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#AnnotationProperty" );
    
    public static final Resource Restriction = m_model.createResource( "http://www.w3.org/2002/07/owl#Restriction" );
    
    public static final Resource Class = m_model.createResource( "http://www.w3.org/2002/07/owl#Class" );
    
    public static final Resource OntologyProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#OntologyProperty" );
    
    public static final Resource ObjectProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#ObjectProperty" );
    
    public static final Resource FunctionalProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#FunctionalProperty" );
    
    public static final Resource InverseFunctionalProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#InverseFunctionalProperty" );
    
    public static final Resource Nothing = m_model.createResource( "http://www.w3.org/2002/07/owl#Nothing" );
    

    // Vocabulary individuals
    ///////////////////////////

}

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


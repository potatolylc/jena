/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            6 Sept 2001
 * Filename           $RCSfile: StatementObjectIterator.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:21:23 $
 *               by   $Author: bwm $
 *
 * (c) Copyright Hewlett-Packard Company 2001
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
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.iterator;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFException;

import java.util.NoSuchElementException;


/**
 * A wrapper for StmtIterator that projects the object of the statement to form
 * a NodeIterator.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: StatementObjectIterator.java,v 1.1.1.1 2002-12-19 19:21:23 bwm Exp $
 */
public class StatementObjectIterator
    implements NodeIterator
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The encapsulated statement iterator */
    private StmtIterator m_sIterator = null;


    // Constructors
    //////////////////////////////////

    /**
     * Construct an iterator to project the object of the statement out to form
     * a node iterator.
     *
     * @param sIterator The StmtIterator whose objects will form the elements of the
     *                  the node iterator.
     */
    public StatementObjectIterator( StmtIterator sIterator ) {
        m_sIterator = sIterator;
    }


    // External signature methods
    //////////////////////////////////

    /**
     * Determine if there any more Nodes in the iteration.
     *
     * @throws RDFException Generic RDF exception.
     * @return true if and only if there are more Resources available
     * from the iteration.
     */
    public boolean hasNext()
        throws RDFException
    {
        return m_sIterator.hasNext();
    }


    /**
     * Return the next Node of the iteration.
     *
     * @throws NoSuchElementException if there are no more to be returned.
     * @throws RDFException Generic RDF exception.
     * @return The next Resource from the iteration.
     */
    public RDFNode next()
        throws  NoSuchElementException, RDFException
    {
        return m_sIterator.next().getObject();
    }


    /**
     * Unsupported Operation.
     * @throws NoSuchElementException
     * @throws RDFException
     */
    public void remove()
        throws NoSuchElementException, RDFException
    {
        m_sIterator.remove();
    }


    /**
     * Terminate the iteration and free up resources.
     *
     * <p>Some implementations, e.g. on relational databases, hold resources while
     * the iterator still exists.  These will normally be freed when the iteration
     * completes.  However, if an application wishes to ensure they are freed without
     * completing the iteration, this method should be called.</p>
     *
     * @throws RDFException Generic RDF exception.
     */
    public void close()
        throws RDFException
    {
        m_sIterator.close();
    }


    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}

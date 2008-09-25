/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.TableDesc;

public abstract class TupleLoaderBase extends com.hp.hpl.jena.sdb.store.TupleLoaderBase implements TupleLoaderBasics {
	
	PreparedStatement insertTupleLoader;
    PreparedStatement insertNodeLoader;
    String insertNodes;
    String insertTuples;
    PreparedStatement deleteTuples;
    PreparedStatement deleteAllTuples;
    PreparedStatement clearTupleLoader;
    PreparedStatement clearNodeLoader;
	
    int chunkSize;
    boolean amLoading; // flag for whether we're loading or deleting
    int tupleNum;
    
    Set<Long> seenNodes; // For suppressing duplicate nodes
    
	public TupleLoaderBase(SDBConnection connection,
			TableDesc tableDesc, int chunkSize) {
		super(connection, tableDesc);
		this.chunkSize = chunkSize;
		this.amLoading = true;
		this.tupleNum = 0;
		this.seenNodes = new HashSet<Long>();
		try {
			init();
		} catch (SQLException e) {
			throw new SDBException("Problem initialising loader for [" + tableDesc + "]", e);
		}
	}
	
	protected void init() throws SQLException {
		// Create the temporary tables
		try {
			if (!TableUtils.hasTable(connection().getSqlConnection(), getNodeLoader()))
				connection().exec(getCreateTempNodes());
			if (!TableUtils.hasTable(connection().getSqlConnection(), getTupleLoader()))
				connection().exec(getCreateTempTuples());
		} catch (SQLException e) { 
		    // Work around for MySQL issue, which won't say if temp table exists
		    // This is also the case for MS Server SQL
		    // Testing the message is as good as it gets without needing the DB-specific 
		    String msg = e.getMessage() ;
		    String className = e.getClass().getName() ;
		    
		    boolean ignore = false ;

		    // MS-SQL
		    if ( className.equals("com.microsoft.sqlserver.jdbc.SQLServerException")
		        && msg.matches("There is already an object named '#.*' in the database."))
		        ignore = true ;

		    // MySQL
		    if ( msg.matches("Table.*already exists") )
		        ignore = true ;

		    if ( ! ignore )
		        throw e;
		}
		
		// Prepare those statements
		insertNodeLoader = connection().prepareStatement(getInsertTempNodes());
		insertTupleLoader = connection().prepareStatement(getInsertTempTuples());
		insertNodes = getLoadNodes();
		insertTuples = getLoadTuples();
		deleteTuples = connection().prepareStatement(getDeleteTuples());
		deleteAllTuples = connection().prepareStatement(getDeleteAllTuples());
		clearNodeLoader = connection().prepareStatement(getClearTempNodes());
		clearTupleLoader = connection().prepareStatement(getClearTempTuples());
	}
	
	public int getArity() {
		return this.getTableWidth();
	}
	
	public void load(Node... row) {
		if (!amLoading) {
			flush();
			amLoading = true;
		}
		
		if (row.length != this.getTableWidth())
			throw new IllegalArgumentException("Tuple size mismatch");
		
		try {
			for (int i = 0; i < row.length; i++) {
				PreparedNode pNode = new PreparedNode(row[i]);
				if (seenNodes.add(pNode.hash)) // if true, this is new...
					pNode.addToStatement(insertNodeLoader);
				insertTupleLoader.setLong(i + 1, pNode.hash);
			}
			insertTupleLoader.addBatch();
		} catch (SQLException e) {
			throw new SDBException("Problem adding to prepared loader statements", e);
		}
		
		tupleNum++;
		if (tupleNum >= chunkSize) flush();
	}

	public void unload(Node... row) {
		if (amLoading) {
			flush();
			amLoading = false;
		}
		
		// Overloading unload, so this is messy
		// If arity mismatch then see if this is a massDelete
		// TODO rethink overloading
		if (row.length != this.getTableWidth()) {
			if ((row.length == 0 && this.getTableWidth() == 3) ||
					(row.length == 1)) {
				massDelete(row);
				return;
			}
			else {
				throw new IllegalArgumentException("Tuple size mismatch");
			}
		}
		
		try {
			for (int i = 0; i < row.length; i++) {
				PreparedNode pNode = new PreparedNode(row[i]); //, false);
				deleteTuples.setLong(i + 1, pNode.hash);
			}
			deleteTuples.addBatch();
		} catch (SQLException e) {
			throw new SDBException("Problem adding to prepared delete statements", e);
		}
		
		tupleNum++;
		if (tupleNum >= chunkSize) flush();
	}
	
	private void massDelete(Node... row) {
		flush();
		boolean handleT = startTransaction(connection());
		try {
			if (row.length == 0) deleteAllTuples.execute();
			else {
				PreparedNode pNode = new PreparedNode(row[0]);
				deleteAllTuples.setLong(1, pNode.hash);
				deleteAllTuples.addBatch();
				deleteAllTuples.executeBatch();
				deleteAllTuples.clearBatch();
			}
			endTransaction(connection(), handleT);
		} catch (SQLException e) {
			if (handleT) {
				try {
					connection().getSqlConnection().rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			throw new SDBException("Exception mass deleting", e);
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		flush();
	}
	
	// Start a transaction if required
	private static boolean startTransaction(SDBConnection connection) {
		boolean handleTransaction = false; // is somebody handling transactions already?
		try {
			handleTransaction = connection.getSqlConnection().getAutoCommit();
			if (handleTransaction) connection.getSqlConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new SDBException("Failed to get autocommit status", e);
		}
		return handleTransaction;
	}
	
	// Complete transaction
	private static void endTransaction(SDBConnection connection, boolean handle) throws SQLException {
		if (!handle) return;
		connection.getSqlConnection().commit();
		connection.getSqlConnection().setAutoCommit(true); // back on
	}
	
	protected void flush() {
		if (tupleNum == 0) return;
		
		boolean handleTransaction = startTransaction(connection());
		
		try {
			if (amLoading) {
				insertNodeLoader.executeBatch();
				insertTupleLoader.executeBatch();
				connection().execUpdate(insertNodes);
				connection().execUpdate(insertTuples);
				if (!handleTransaction || !clearsOnCommit()) {
					clearNodeLoader.execute();
					clearTupleLoader.execute();
				}
			} else {
				deleteTuples.executeBatch();
			}
			
			endTransaction(connection(), handleTransaction);
		} catch (SQLException e) {
			if (handleTransaction)
				try {
					connection().getSqlConnection().rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			throw new SDBException("Exception flushing", e);
		} finally {
			tupleNum = 0;
			seenNodes = new HashSet<Long>();
		}
	}
	
	/** These are the SQL 'bits' we use to construct the loader statements **/
	
	public String getNodeLoader() {
		return "NNode" + this.getTableName();
	}
    
	public String getTupleLoader() {
		return "N" + this.getTableName();
	}
	
	public String getCreateTempNodes() {
		StringBuilder stmt = new StringBuilder();
		
		String[] tempBookends = getCreateTempTable();
		
		stmt.append(tempBookends[0]).append(" ").append(getNodeLoader()).append(" \n(");
		
		String[] nodeColTypes = getNodeColTypes();
		
		for (int i = 0; i < nodeColTypes.length; i++) {
			if (i != 0) stmt.append(" , \n");
			stmt.append("n").append(i).append(" ").append(nodeColTypes[i]);
		}
		stmt.append("\n) ").append(tempBookends[1]);
		
		return stmt.toString();
	}
	
	public String getCreateTempTuples() {
		StringBuilder stmt = new StringBuilder();
		
		String[] tempBookends = getCreateTempTable();
		
		stmt.append(tempBookends[0]).append(" ").append(getTupleLoader()).append(" \n(");
		
		int width = this.getTableWidth();
		
		for (int i = 0; i < width; i++) {
			if (i != 0) stmt.append(" , \n");
			stmt.append("t").append(i).append(" ").append(getTupleColType());
		}
		stmt.append("\n) ").append(tempBookends[1]);
		
		return stmt.toString();
	}
	
	public String getInsertTempNodes() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO ").append(getNodeLoader()).append(" VALUES (");
		for (int i = 0; i < getNodeColTypes().length; i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append("?");
		}
		stmt.append(" )");
		
		return stmt.toString();
	}
	
	public String getInsertTempTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO ").append(getTupleLoader()).append(" VALUES (");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append("?");
		}
		stmt.append(" )");
		
		return stmt.toString();
	}
	
	public String getLoadNodes() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO Nodes (hash, lex, lang, datatype, type) \nSELECT ");
		for (int i = 0; i < getNodeColTypes().length; i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append(getNodeLoader()).append(".").append("n").append(i);
		}
		stmt.append("\nFROM ").append(getNodeLoader()).append(" LEFT JOIN Nodes ON (");
		stmt.append(getNodeLoader()).append(".n0=Nodes.hash) \nWHERE Nodes.hash IS NULL"); 
		return stmt.toString();
	}
	
	public String getClearTempNodes() {
		return "DELETE FROM " + getNodeLoader();
	}
	
	public String getClearTempTuples() {
		return "DELETE FROM " + getTupleLoader();
	}
	
	public boolean clearsOnCommit() { return false; }
	
	/* Encapsulate the gory internals of breaking up nodes for the database */
	
	public static class PreparedNode
    {
        public long hash;
        public String lex;
        public String lang;
        public String datatype;
        public int typeId;
        //public Integer valInt;
        //public Double valDouble;
        //public Timestamp valDateTime;
        
        //PreparedNode(Node node)
        //{
        //	this(node, true);
        //}
        
        PreparedNode(Node node) //, boolean computeVals)
        {
            lex = NodeLayout2.nodeToLex(node);
            //ValueType vType = ValueType.lookup(node);
            typeId = NodeLayout2.nodeToType(node);

            lang = "";
            datatype = "";

            if (node.isLiteral())
            {
                lang = node.getLiteralLanguage();
                datatype = node.getLiteralDatatypeURI();
                if (datatype == null)
                    datatype = "";
            }

            hash = NodeLayout2.hash(lex, lang, datatype, typeId);
            
            /*if (computeVals) // don't need this for deleting
            {
            	// Value of the node
            	valInt = null;
            	if (vType == ValueType.INTEGER)
            		valInt = Integer.parseInt(lex);
            	
            	valDouble = null;
            	if (vType == ValueType.DOUBLE)
            		valDouble = Double.parseDouble(lex);
            	
            	valDateTime = null;
            	if (vType == ValueType.DATETIME)
            	{
            		String dateTime = SQLUtils.toSQLdatetimeString(lex);
            		valDateTime = Timestamp.valueOf(dateTime);
            	}
            }*/
        }
        
        public void addToStatement(PreparedStatement s)
        	throws SQLException
        {
        	s.setLong(1, hash);
        	s.setString(2, lex);
        	s.setString(3, lang);
        	s.setString(4, datatype);
        	s.setInt(5, typeId);
        	/*if (valInt != null)
        		s.setInt(6, valInt);
        	else
        		s.setNull(6, Types.INTEGER);
        	if (valDouble  != null)
        		s.setDouble(7, valDouble);
        	else
        		s.setNull(7, Types.DOUBLE);
        	if (valDateTime != null)
        		s.setTimestamp(8, valDateTime);
        	else
        		s.setNull(8, Types.TIMESTAMP);*/
        	s.addBatch();
        }
        
        @Override
        public int hashCode()
        {
        	return (int) (hash & 0xFFFF);
        }
        
        @Override
        public boolean equals(Object other)
        {
        	return ((PreparedNode) other).hash == hash;
        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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

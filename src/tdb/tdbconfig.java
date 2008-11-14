/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.List;

import tdb.cmdline.CmdSub;
import tdb.cmdline.CmdTDB;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModVersion;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileDisk;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector;
import com.hp.hpl.jena.tdb.store.GraphTDB;

/** Tools to manage a TDB store.  Subcommand based. */
public class tdbconfig extends CmdSub
{
    static final String CMD_CLEAN   = "clean" ;
    static final String CMD_HELP    = "help" ;
    static final String CMD_STATS   = "stats" ;
    static final String CMD_NODES   = "nodes" ;
    static final String CMD_INFO    = "info" ;
    
    static public void main(String... argv)
    {
        new tdbconfig(argv).exec();
    }

    protected tdbconfig(String[] argv)
    {
        super(argv) ;
//        super.addSubCommand(CMD_CLEAN, new Exec()
//          { @Override public void exec(String[] argv) { new tdbclean(argv).main() ; } }) ;
        
        super.addSubCommand(CMD_HELP, new Exec()
        { @Override public void exec(String[] argv) { new SubHelp(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_STATS, new Exec()
        { @Override public void exec(String[] argv) { new SubStats(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_NODES, new Exec()
        { @Override public void exec(String[] argv) { new SubNodes(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_INFO, new Exec()
        { @Override public void exec(String[] argv) { new SubInfo(argv).mainRun() ; } }) ;

        
    }
    
    // Subcommand : help
    static class SubHelp extends CmdARQ
    {
        public SubHelp(String ... argv)
        {
            super(argv) ;
            //super.addModule(modSymbol) ;
        }
        
        @Override
        protected String getSummary()
        {
            return null ;
        }

        @Override
        protected void exec()
        {
            System.out.println("Help!") ;
        }

        @Override
        protected String getCommandName()
        {
            return "tdbconfig help" ;
        }
    }
    
    static class SubStats extends CmdTDB
    {
        public SubStats(String ... argv)
        {
            super(argv) ;
            //super.addModule(modSymbol) ;
        }
        
        @Override
        protected String getSummary()
        {
            return "tdbconfig stats" ;
        }

        @Override
        protected void exec()
        {
            GraphTDB graph = getGraph() ;
            Item item = StatsCollector.gatherTDB(graph) ;
            System.out.println(item) ;
        }

        @Override
        protected String getCommandName()
        {
            return "tdbconfig stats" ;
        }
    }
    
    static class SubNodes extends CmdTDB
    {
        public SubNodes(String ... argv)
        {
            super(argv) ;
        }
        
        @Override
        protected String getSummary()
        {
            return "tdbconfig stats" ;
        }

        @Override
        protected void exec()
        {
            @SuppressWarnings("unchecked")
            List<String> args = (List<String>)positionals ;
            for ( String x : args )
            {
                System.out.println("**** Object File: "+x) ;
                ObjectFileDisk objs = new ObjectFileDisk(x) ;
                objs.dump() ;
            }
        }

        @Override
        protected String getCommandName()
        {
            return "tdbconfig nodes" ;
        }
    }

    static class SubInfo extends CmdTDB
    {
        public SubInfo(String ... argv)
        {
            super(argv) ;
        }
        
        @Override
        protected String getSummary()
        {
            return "tdbconfig info" ;
        }

        @Override
        protected void exec()
        {
            System.out.println("-- "+Utils.nowAsString()+" --") ;
            ModVersion v = new ModVersion(true) ;
            v.addClass(TDB.class) ;
            v.printVersionAndExit() ;
        }

        @Override
        protected String getCommandName()
        {
            return "tdbconfig info" ;
        }
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
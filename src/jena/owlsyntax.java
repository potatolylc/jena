/*
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena;

import jena.cmdline.*;

import java.util.*;
import com.hp.hpl.jena.ontology.tidy.test.WGTests;
import com.hp.hpl.jena.ontology.tidy.*;
import com.hp.hpl.jena.shared.*;
import java.io.*;

/**
    Implements the OWL Syntax Checker from the OWL Test Cases.
    Current known weaknesses:
    <ul>
    <li>Does not report "Other" for non-RDF/XML files</li>
    <li>Network error reporting on bad owl:imports has not been tested.</li>
    </ul>
    When two files are given on the command-line it treats the
    first as a premises file and the second as a conclusion file
    and indicates the sort of OWL reasoner that may be needed
    for testing the entailment. For example, if both files are
    Lite but a URI is used as an individual in the first
    and a class in the second then a Full reasoner is needed.
 * @author		Jeremy Carroll
 * @version 	$Id: owlsyntax.java,v 1.2 2004-01-27 15:45:24 jeremy_carroll Exp $
 */
public class owlsyntax {
	private owlsyntax(){
		// No javadoc
	}

	static final String NL = System.getProperty("line.separator", "\n");


	private static ArgDecl quietDecl = new ArgDecl(false, "-q", "--quiet");
	private static ArgDecl helpDecl = new ArgDecl(false, "-h", "--help");
	private static ArgDecl liteDecl = new ArgDecl(false, "-l", "--lite");
	private static ArgDecl bigDecl = new ArgDecl(false, "-b", "--big");
	private static ArgDecl shortDecl = new ArgDecl(false, "-s", "--short");
	private static ArgDecl textUIDecl = new ArgDecl(false, "--textui");
	private static ArgDecl testDecl = new ArgDecl(false, "--test");
	private static String usageMessage =
		"    " + owlsyntax.class.getName()
			+ " [--lite|--quiet] [--big] [file1] [file2]"
			+ NL
			+ "    " + owlsyntax.class.getName()
			+ " --help"
			+ NL
			+ "    " + owlsyntax.class.getName()
			+ " [--textui] --test [ManifestURL]"
			+ NL + NL 
			+ "The first form reports \"Lite\", \"DL\", or \"Full\""
			+ NL
			+ "If two files are specified, then both files are checked, and"
			+ NL
			+ "the vocabulary usage by both files together must be separated."
			+ NL
			+ "If no files are specified then standard input is used,"
			+ NL
			+ "(in this case, relative URIs and rdf:ID's are resolved"
			+ NL
			+ " against <urn:x-jena:syntaxchecker>)."
			+ NL + NL
			+ "  -l --lite     Give error messages for OWL DL or OWL Full constructions."
			+ NL
			+ "  -q --quiet    No error messages."
			+ NL
			+ "  -s --short    Give short error messages"
			+ NL
			+ "                (Default is long messages for OWL Full only)"
			+ NL
			+ "  -b --big      Input file is big - optimize memory usage."
			+ NL
			+ "                Quality of long error messages suffers."
			+ NL + NL
			+ "  --test        Run a test suite - default latest OWL Test publication."
			+ NL
			+ "                URL of file:testing/wg/OWLManifest.rdf uses local copy."
			+ NL
			+ "  --textui      Use the junit.textui instead of the swingui"
			+ NL;
	/**
	 * Run the OWL Syntax Checker.
	 * Usage message is:
	 * <pre>
   jena.owlsyntax [--lite|--quiet] [--big] [file1] [file2]
   jena.owlsyntax --help
   jena.owlsyntax [--textui] --test [ManifestURL]

The first form reports "Lite", "DL", or "Full"
If two files are specified, then both files are checked, and
the vocabulary usage by both files together must be separated.
If no files are specified then standard input is used,
(in this case, relative URIs and rdf:ID's are resolved
 against <urn:x-jena:syntaxchecker>).

  -l --lite     Give error messages for OWL DL or OWL Full constructions.
  -q --quiet    No error messages.
  -s --short    Give short error messages
				(Default is long messages for OWL Full only)
  -b --big      Input file is big - optimize memory usage.
				Quality of long error messages suffers.

  --test        Run a test suite - default latest OWL Test publication.
				URL of file:testing/wg/OWLManifest.rdf uses local copy.
  --textui      Use the junit.textui instead of the swingui
	 * </pre>
	 * @param args
	 */
	public static void main(String[] args) {

		CommandLine cmd = new CommandLine();
		cmd.setUsage(usageMessage);
		cmd.setOutput(System.err);

		cmd
			.add(quietDecl)
			.add(helpDecl)
			.add(liteDecl)
			.add(bigDecl)
			.add(shortDecl)
			.add(textUIDecl)
			.add(testDecl);

		try {
			cmd.process(args);
		} catch (IllegalArgumentException illEx) {
			System.exit(1);
		}

		boolean testing = cmd.contains(testDecl) || cmd.contains(textUIDecl);
		boolean running =
			(!testing)
				|| cmd.contains(quietDecl)
				|| cmd.contains(shortDecl)
				|| cmd.contains(liteDecl)
				|| cmd.contains(bigDecl);
		boolean bad =
			testing
				? (running || cmd.items().size() > 1)
				: (cmd.items().size() > 2)
				|| (cmd.contains(liteDecl) && cmd.contains(quietDecl));
		if (cmd.contains(helpDecl) || bad) {
			System.out.println(usageMessage);
			System.exit(bad ? 1 : 0);
		}
		if (testing) {
			String manifest = "http://www.w3.org/TR/owl-test/Manifest";
			if (cmd.items().size() > 0) {
				manifest = (String) cmd.items().get(0);
			}
			WGTests.test(cmd.contains(textUIDecl), manifest);
		} else {
			StreamingChecker chk = new StreamingChecker(cmd.contains(liteDecl));
			boolean big =
				cmd.contains(bigDecl)
					|| cmd.contains(quietDecl)
					|| cmd.contains(shortDecl);
			chk.setOptimizeMemory(big);

			switch (cmd.items().size()) {
				case 0 :
					check(chk, System.in, "", cmd);
					break;
				case 1 :
					check(chk, (String) cmd.items().get(0), "", cmd);
					break;
				case 2 :
					check(
						chk,
						(String) cmd.items().get(0),
						(String) cmd.items().get(0),
						cmd);
					StreamingChecker chk2 = new StreamingChecker(cmd.contains(liteDecl));
					chk2.setOptimizeMemory(big);
					check(
						chk2,
						(String) cmd.items().get(1),
						(String) cmd.items().get(1),
						cmd);
					check(chk, (String) cmd.items().get(1), "togther", cmd);
					break;
				default :
					throw new BrokenException("Logic error");
			}
		}
	}
	static private void check(
		StreamingChecker chk,
		InputStream in,
		String msgPrefix,
		CommandLine cmd) {
		chk.load(in, "urn:x-jena:syntaxchecker");
		results(chk, msgPrefix, cmd);
	}
	static private void check(
		StreamingChecker chk,
		String url,
		String msgPrefix,
		CommandLine cmd) {
		chk.load(url);
		results(chk, msgPrefix, cmd);
	}
	static private String prefix(String m) {
		if (m.length() == 0)
			return m;
		else
			return m + ": ";
	}
	static private void results(
		CheckerResults chk,
		String msgPrefix,
		CommandLine cmd) {
		String subLang = chk.getSubLanguage();
		String p = prefix(msgPrefix);
		System.out.println(p + subLang);
		if (cmd.contains(quietDecl))
			return;

		Iterator it = chk.getProblems();
		while (it.hasNext()) {
			SyntaxProblem sp = (SyntaxProblem) it.next();
			System.err.println(
				p
					+ (cmd.contains(shortDecl)
						? sp.shortDescription()
						: sp.longDescription()));
		}

	}
}

/*
 *  (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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

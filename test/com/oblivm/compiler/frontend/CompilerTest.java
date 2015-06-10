/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.parser.CParser;
import com.oblivm.compiler.parser.ParseException;
import com.oblivm.compiler.type.manage.RecordType;
import com.oblivm.compiler.type.manage.Type;
import com.oblivm.compiler.type.source.TypeChecker;

public class CompilerTest {

	public static void compile(String source, String target) throws FileNotFoundException, ParseException {
		CParser parser = new CParser(new FileInputStream(source));
		ASTProgram program = parser.TranslationUnit();
		TypeChecker tc = new TypeChecker();
		System.out.println("Type Checking... "+tc.check(program));
		FrontendCompiler compiler = new FrontendCompiler();
		for(Type type : compiler.translate(program).getTypes())
			System.out.println(((RecordType)type).fullSpec());
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ParseException {
//		compile("input/kmpfind.lcc", "KMPFind");
//		compile("input/inversepair.lcc", "InversePair");
//		compile("input/stream1.lcc", "Stream1");
//		compile("input/dijkstra.lcc", "Dijstra");
//		compile("input/dijkstra.lcc", "Dijkstra");
//		compile("input/inversepermutation.lcc", "Inverse");
//		compile("input2/oramstack.lcc", "Binary");
//		compile("input2/bug4.lcc", "Bug4");
//		compile("input/noarray.lcc", "Noarray");
//		compile("input/oramsimple.lcc", "ORAMSimple");
		compile("examples/input2/matrix.lcc", "Matrix");	
//		compile("input/heapExtract.lcc", "HeapExtract");
		
	}

}

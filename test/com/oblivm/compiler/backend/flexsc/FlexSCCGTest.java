/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend.flexsc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.frontend.FrontendCompiler;
import com.oblivm.compiler.parser.CParser;
import com.oblivm.compiler.parser.ParseException;

public class FlexSCCGTest {

	public static void compile(String source) throws 
	IOException, ParseException {
		FlexSCCodeGenerator compiler = new FlexSCCodeGenerator();
		CParser parser = new CParser(new FileInputStream(source));
		ASTProgram program = parser.TranslationUnit();
		
		compiler.FlexSCCodeGen(new FrontendCompiler().compile(program),
				new Config(), true, false);
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
//		compile("input2/cpu.lcc"); // TESTED
//		compile("input2/stack.lcc"); // TESTED
//		compile("input2/simpleloop.lcc"); // TESTED
//		compile("input2/bug2.lcc"); // TESTED
//		compile("input2/oramstack.lcc"); // TESTED
//		compile("input2/pq.lcc"); // TESTED
//		compile("input2/ams_sketch.lcc"); // TESTED
//		compile("input2/oramstack.lcc"); // TESTED
//		compile("input2/priority_queue.lcc");
//		compile("input2/matrix.lcc");
//		compile("input2/avl.lcc"); // TESTED
//		compile("input2/avl_withoutstack.lcc"); // TESTED
//		compile("input3/leadingZero.lcc"); // TESTED
//		compile("input3/function.lcc"); // TESTED
		compile("input3/nstack.lcc"); // TESTED
//		compile("input2/mapreduce.lcc"); // TESTED
	}
}

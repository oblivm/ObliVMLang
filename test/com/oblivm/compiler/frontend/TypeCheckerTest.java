/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend;

import java.io.FileInputStream;

import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.parser.CParser;
import com.oblivm.compiler.type.source.TypeChecker;

public class TypeCheckerTest {

	public void test(String file) throws Exception {
		CParser parser = new CParser(new FileInputStream(file));
		ASTProgram program = parser.TranslationUnit();
		System.out.println(program.toString());
		TypeChecker tc = new TypeChecker();
		System.out.println(tc.check(program));
	}

	public static void main(String[] args) throws Exception {
		//		new TypeCheckerTest().test("examples/input2/abb.lcc");
		//		new TypeCheckerTest().test("examples/input2/ams_sketch.lcc");
		//		new TypeCheckerTest().test("examples/input2/avl_withoutstack.lcc");
//		new TypeCheckerTest().test("examples/input2/avl.lcc");
//		new TypeCheckerTest().test("examples/input2/count_min_sketch.lcc");
//		new TypeCheckerTest().test("examples/input2/cpu.lcc");
//		new TypeCheckerTest().test("examples/input2/mapreduce.lcc");
//		new TypeCheckerTest().test("examples/input2/matrix.lcc");
//		new TypeCheckerTest().test("examples/input2/nstack.lcc");
//		new TypeCheckerTest().test("examples/input2/oramstack.lcc");
//		new TypeCheckerTest().test("examples/input2/pq.lcc");
//		new TypeCheckerTest().test("examples/input2/priority_queue.lcc");
//		new TypeCheckerTest().test("examples/input2/simpleloop.lcc");

//		new TypeCheckerTest().test("examples/input3/bs.lcc");
		new TypeCheckerTest().test("examples/input3/CircuitORAM.lcc");
//		new TypeCheckerTest().test("examples/input3/function.lcc");
//		new TypeCheckerTest().test("examples/input3/graph.lcc");
//		new TypeCheckerTest().test("examples/input3/leadingZero.lcc");
//		new TypeCheckerTest().test("examples/input3/nstack.lcc");
//		new TypeCheckerTest().test("examples/input3/affine_test.lcc");
	}

}

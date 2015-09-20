/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.parser;

import java.io.FileInputStream;

public class ParsingTest {
	
	public static void main(String[] args) throws Exception {
//		System.out.println(Integer.decode("0xffff0000"));
		CParser parser = new CParser(new FileInputStream("input3/CircuitORAM.lcc"));
		System.out.println(parser.TranslationUnit().toString());
	}

}

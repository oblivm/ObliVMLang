/**
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.log;

/**
 * @author Chang Liu
 *
 */
public class Bugs extends Log {
	public static Bugs LOG = new Bugs();
	
	private Bugs() { 
		super(System.err); 
	}

	@Override
	public String tag() {
		return "ERROR";
	}

}

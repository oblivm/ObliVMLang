/**
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.log;

/**
 * @author Chang Liu
 *
 */
public class Debug extends Log {
	public static Debug LOG = new Debug();
	
	private Debug() { 
		super(System.err); 
	}

	@Override
	public String tag() {
		return "DEBUG";
	}

}

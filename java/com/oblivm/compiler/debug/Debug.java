/**
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.debug;

/**
 * @author Chang Liu
 *
 */
public class Debug {
	public static Debug LOG = new Debug();
	
	private Debug() {}
	
	public void log(String msg) {
		System.err.println(msg);
	}
}

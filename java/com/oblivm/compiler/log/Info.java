/**
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.log;

/**
 * @author Chang Liu
 *
 */
public class Info extends Log {
	public static Info LOG = new Info();
	
	private Info() { 
		super(System.out); 
	}

	@Override
	public String tag() {
		return "INFO";
	}

}

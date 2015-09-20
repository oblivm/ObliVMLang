/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend.bloop;


public class Label {
	public final static Label Next = new Label();
	
	private String name;
	
	private Label() {
		name = "Next";
	}
	
	public Label(String name) {
		if(name.equals("Next"))
			throw new RuntimeException("Please use Label.Next instead of creating a new next label.");
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static int total = 0;
	
	public static Label newLabel() {
		Label ret = new Label("label"+(total ++));
		return ret;
	}
	
	public int getId() {
		if(name.startsWith("label"))
			return Integer.parseInt(name.substring(5));
		else
			return -1;
	}
	public String toString() {
		return "L" + name;
	}
}

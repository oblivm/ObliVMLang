/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.util;

public class Pair<T1, T2> {
	public T1 left;
	public T2 right;
	
	public Pair(T1 left, T2 right) {
		this.left = left;
		this.right = right;
	}
	
	public String toString() {
		return "("+left+" , "+right+")";
	}
}

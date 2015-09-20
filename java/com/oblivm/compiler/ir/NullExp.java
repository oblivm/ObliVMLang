/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class NullExp extends Expression {
	public static NullExp inst = new NullExp();
	
	private NullExp() {}
	
	@Override
	public String toString() {
		return "null";
	}

	@Override
	public Label getLabels() {
		return Label.Pub;
	}

}

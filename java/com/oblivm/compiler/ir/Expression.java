/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public abstract class Expression {
	public abstract String toString();
	
	public abstract Label getLabels();
}

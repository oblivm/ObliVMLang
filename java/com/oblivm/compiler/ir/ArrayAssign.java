/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class ArrayAssign extends IRCode {
	
	public Label indexLab;
	
	public Variable name;

	public Variable idx;

	public Variable value;
	
	public ArrayAssign(Label lab, Variable arr, Variable idx, Variable value) {
		this.indexLab = lab;
		this.name = arr;
		this.idx = idx;
		this.value = value;
	}

	@Override
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer(this.indent(indent));
		if(indexLab == Label.Secure) {
			sb.append(name.name);
			sb.append(".Write(");
			sb.append(idx.toString());
			sb.append(',');
			sb.append(value.toString());
			sb.append(");\n");
		} else {
			sb.append(name.name+"["+idx.toString()+"]="+value.toString()+";\n");
		}
		return sb.toString();
	}

	@Override
	public IRCode clone(boolean withTypeDef) {
		IRCode ret = new ArrayAssign(indexLab, name, idx, value);
		ret.withTypeDef = withTypeDef;
		return ret;
	}

}

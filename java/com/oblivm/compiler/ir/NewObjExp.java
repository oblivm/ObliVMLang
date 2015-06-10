/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import java.util.Map;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.RecordType;

public class NewObjExp extends Expression {
	public RecordType type;
	public Label lab;
	public Map<String, Variable> initialValue;
	
	public NewObjExp(RecordType type, Label lab, Map<String, Variable> initValue) {
		this.type = type;
		this.lab = lab;
		this.initialValue = initValue;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(type.name+"(");
		boolean f = true;
		for(Map.Entry<String, Variable> ent : initialValue.entrySet()) {
			if(f) f = false;
			else
				sb.append(", ");
			sb.append(ent.getKey()+" = "+ent.getValue());
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Label getLabels() {
		return lab;
	}

}

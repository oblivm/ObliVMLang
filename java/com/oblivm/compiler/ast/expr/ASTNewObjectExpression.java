/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

import java.util.HashMap;
import java.util.Map;

import com.oblivm.compiler.ast.type.ASTRecType;

public class ASTNewObjectExpression extends ASTExpression {
	// TODO Currently support new struct only
	public ASTRecType type;
	public Map<String, ASTExpression> valueMapping;
	
	public ASTNewObjectExpression cloneInternal() {
		Map<String, ASTExpression> values = new HashMap<String, ASTExpression>();
		for(Map.Entry<String, ASTExpression> ent : valueMapping.entrySet()) {
			values.put(ent.getKey(), ent.getValue().clone());
		}
		return new ASTNewObjectExpression(type, values);
	}
	
	public ASTNewObjectExpression(ASTRecType type, Map<String, ASTExpression> initialValue) {
		this.type = type;
		this.valueMapping = initialValue;
	}

	@Override
	public int level() {
		return 100;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(type.shortName());
		sb.append("(");
		boolean f = true;
		for(Map.Entry<String, ASTExpression> ent : this.valueMapping.entrySet()) {
			if(f) f = false;
			else sb.append(", ");
			sb.append(ent.getKey()+" = "+ent.getValue());
		}
		sb.append(")");
		return sb.toString();
	}
}

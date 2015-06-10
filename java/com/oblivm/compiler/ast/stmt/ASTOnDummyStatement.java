/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.stmt;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.util.Pair;

public class ASTOnDummyStatement extends ASTStatement {
	public static class Condition {
		private String name;
		private Condition() { }
		private Condition(String name) { this.name = name; }
		public static Condition OnReal = new Condition("ONREAL");
		public static Condition OnDummy = new Condition("ONDUMMY");
		
		public String toString() {
			return name;
		}
	}
	
	public Condition cond;
	public List<Pair<String, ASTExpression>> condList;
	public List<ASTStatement> body;
	
	public ASTOnDummyStatement(Condition cond) {
		this.cond = cond;
		condList = new ArrayList<Pair<String, ASTExpression>>();
		body = new ArrayList<ASTStatement>();
	}
	
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.indent(indent));
		sb.append(cond.name+"(");
		for(int i=0; i<condList.size(); ++i) {
			if(i > 0) sb.append(",");
			if(condList.get(i).left != null) {
				sb.append(condList.get(i).left+" = ");
			}
			sb.append(condList.get(i).right.toString());
		}
		sb.append(") {\n");
		++indent;
		for(int i=0; i<body.size(); ++i) {
			sb.append(body.get(i).toString(indent+1));
		}
		sb.append(indent(indent));
		sb.append("}\n");
		return sb.toString();
	}

}

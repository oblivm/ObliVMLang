package com.oblivm.compiler.ast.stmt;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.util.Pair;

public class ASTUsingStatement extends ASTStatement {

	public List<Pair<String, ASTExpression>> use = new ArrayList<Pair<String, ASTExpression>>();
	public List<ASTStatement> body = new ArrayList<ASTStatement>();
	
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.indent(indent));
		sb.append("using (");
		boolean f = true;
		for(Pair<String, ASTExpression> ent : use) {
			if(f) f = false; else sb.append("; ");
			sb.append(ent.left);
			sb.append(" = ");
			sb.append(ent.right.toString());
		}
		sb.append(") {\n");
		for(ASTStatement stmt : body) {
			sb.append(stmt.toString(indent + 1));
		}
		sb.append(this.indent(indent)+"}\n");
		return sb.toString();
	}
	
	public String toString() {
		return toString(0);
	}


}

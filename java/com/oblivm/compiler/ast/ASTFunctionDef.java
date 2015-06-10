/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.stmt.ASTStatement;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.util.Pair;

public class ASTFunctionDef extends ASTFunction {
	
	public ASTFunctionDef(boolean isDummy, String name, ASTType returnType, ASTType baseType,
			List<String> bitParameter,
			List<ASTVariableType> typeVariables,
			List<Pair<ASTType, String>> inputs) {
		super(isDummy, name, returnType, baseType, bitParameter, typeVariables, inputs);
		this.body = null;
		this.localVariables = new ArrayList<Pair<ASTType, String>>();
	}

	/***
	 * Check if the function has a local variable or an input variable (but not a using block variable) with a given name 
	 * @return
	 */
	public boolean containVariables(String var) {
		for(Pair<ASTType, String> ent : localVariables)
			if(ent.right.equals(var))
				return true;
		for(Pair<ASTType, String> ent : inputVariables)
			if(ent.right.equals(var))
				return true;
		return false;
	}
	
	public List<Pair<ASTType, String>> localVariables;
	public List<ASTStatement> body;
	
	
	public String toString(int indent) {
		return toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.returnType.shortName()+" "+name);
		for(String bit : this.bitParameter) {
			sb.append("@"+bit);
		}
		sb.append("(");
		boolean flag = true;
		for(int i=0; i<inputVariables.size(); ++i) {
			if(!flag) sb.append(", ");
			else flag = false;
			sb.append(inputVariables.get(i).left.shortName()+" "+inputVariables.get(i).right);
		}
		sb.append(") {\n");
		for(int i=0; i<localVariables.size(); ++i) {
			sb.append(this.indent(1));
			sb.append(localVariables.get(i).left.shortName()+" "+localVariables.get(i).right+";\n");
		}
		for(int i=0; i<body.size(); ++i) {
			sb.append(body.get(i).toString(1));
		}
		sb.append("}\n");
		
		return sb.toString();
	}
}

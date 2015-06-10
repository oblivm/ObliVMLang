/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.util.Pair;

public class ASTFunctionNative extends ASTFunction {
	public String nativeName;
	
	public ASTFunctionNative(boolean isDummy, String name, ASTType returnType, ASTType baseType, List<ASTVariableType> typeVariables,
			List<Pair<ASTType, String>> inputs) {
		super(isDummy, name, returnType, baseType,
				new ArrayList<String>(), // Native function doesn't have bit parameters for now
				typeVariables, inputs);
	}

	@Override
	public String toString(int indent) {
		return toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(isDummy) sb.append("dummy ");
		sb.append(returnType.toString()+" ");
		if(baseType != null) {
			sb.append(baseType+".");
		}
		sb.append(name+"(");
		for(int i=0; i<this.inputVariables.size(); ++i) {
			if(i > 0) sb.append(", ");
			sb.append(this.inputVariables.get(i).left.toString()+" "+this.inputVariables.get(i).right.toString());
		}
		sb.append(") = native ");
		sb.append(nativeName+";\n");
		return sb.toString();
	}
}

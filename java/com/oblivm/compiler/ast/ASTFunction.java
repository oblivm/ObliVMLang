/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.type.ASTFunctionType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.util.Pair;

public abstract class ASTFunction extends AST {
	public List<String> bitParameter;
	public List<ASTVariableType> typeVariables;
	public List<Pair<ASTType, String>> inputVariables;
	public String name;
	public ASTType returnType;

	public ASTType baseType;
	
	public boolean isDummy;
	
	public ASTFunction(boolean isDummy, String name, ASTType returnType, 
			ASTType baseType, List<String> bitParameter, List<ASTVariableType> typeVariables, List<Pair<ASTType, String>> inputs) {
		this.bitParameter = bitParameter;
		this.isDummy = isDummy;
		this.name = name;
		this.returnType = returnType;
		this.baseType = baseType;
		this.inputVariables = inputs;
		this.typeVariables = typeVariables;
	}
	
	public ASTFunctionType getType() {
		List<ASTType> inputs = new ArrayList<ASTType>();
		for(int i=0; i<inputVariables.size(); ++i)
			inputs.add(inputVariables.get(i).left);
		
		ASTFunctionType ret = new ASTFunctionType(returnType, name, inputs, true);
		if(typeVariables != null)
			for(int i=0; i<typeVariables.size(); ++i)
				ret.typeParameter.add(typeVariables.get(i));
		
		for(int i=0; i<bitParameter.size(); ++i)
			ret.bitParameter.add(bitParameter.get(i));
		
		ret.isPhantom = isDummy;
		
		return ret;
	}
}

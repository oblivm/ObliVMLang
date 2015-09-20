/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

public class ASTFunctionType extends ASTType {
	public ASTType returnType;
	public List<ASTType> inputTypes;
	public String name;
	public List<String> bitParameter;
	public List<ASTType> typeParameter;
	public boolean global;
	public boolean isPhantom;
	
	public ASTFunctionType(ASTType ret, 
			String name, 
			List<String> bitParameter, 
			List<ASTType> inputs, 
			boolean global) {
		this.bitParameter = bitParameter;
		this.returnType = ret;
		this.name = name;
		this.inputTypes = inputs;
		this.typeParameter = new ArrayList<ASTType>();
		this.global = global;
	}

	public ASTFunctionType(
			ASTType ret, 
			String name, 
			List<ASTType> inputs, 
			boolean global) {
		this(ret, name, new ArrayList<String>(), inputs, global);
	}
	
	public ASTFunctionType(ASTType ret, 
			String name, 
			boolean global) {
		this(ret, name, new ArrayList<ASTType>(), global);
	}
	
	@Override
	public boolean canFlowTo(ASTType type) {
		if(!(type instanceof ASTFunctionType))
			return false;
		ASTFunctionType ftype = (ASTFunctionType)type;
		if(!returnType.canFlowTo(ftype.returnType) || this.inputTypes.size() != ftype.inputTypes.size())
			return false;
		for(int i=0; i<inputTypes.size(); ++i)
			if(!ftype.inputTypes.get(i).canFlowTo(this.inputTypes.get(i)))
				return false;
		return true;
	}
	@Override
	public String shortName() {
		StringBuffer sb = new StringBuffer();
		sb.append(returnType.shortName());
		sb.append(" "+name);
		if(this.typeParameter != null && this.typeParameter.size() > 0) {
			sb.append("<");
			for(int i=0; i<this.typeParameter.size(); ++i) {
				if(i > 0) sb.append(", ");
				sb.append(this.typeParameter.get(i));
			}
			sb.append(">");
		}
		sb.append("(");
		for(int i=0; i<this.inputTypes.size(); ++i) {
			if(i > 0)
				sb.append(", ");
			sb.append(inputTypes.get(i).shortName());
		}
		sb.append(")");
		return sb.toString();
	}
	@Override
	public String toString(int indent) {
		return this.shortName();
	}
	
	public String toString() {
		return toString(0);
	}

	@Override
	public ASTLabel getLabel() {
		return this.isPhantom ? ASTLabel.Secure : ASTLabel.Pub;
	}
}

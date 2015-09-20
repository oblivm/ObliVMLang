/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ir.IRCode;
import com.oblivm.compiler.util.Pair;

public class Method {
	public Type baseType;
	public Type returnType;
	public String name;
	public List<Type> typeParameters;
	public List<String> bitParameters;
	public List<Pair<Type, String>> parameters;
	public List<Pair<Type, String>> localVariables;
	
	public boolean isPhantom = false;
	
	public IRCode code;
	
	public FunctionType getType() {
		List<Type> inputs = new ArrayList<Type>();
		for(int i=0; i<parameters.size(); ++i)
			inputs.add(parameters.get(i).left);
		List<Type> tP = new ArrayList<Type>();
		if(typeParameters != null)
			for(int i=0; i<typeParameters.size(); ++i)
				tP.add(typeParameters.get(i));
		FunctionType type = new FunctionType(returnType, name, inputs, bitParameters, tP, true);
		return type;
	}
	
	public Method(Type baseType, Type returnType, String name, List<String> bitParameters, List<Pair<Type, String>> para, List<Pair<Type, String>> local) {
		this.bitParameters = bitParameters;
		this.baseType = baseType;
		this.returnType = returnType;
		this.name = name;
		this.parameters = para;
		this.localVariables = local;
		this.code = null;
		this.typeParameters = new ArrayList<Type>();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\t"+returnType.name+" "+name);
		for(String s : this.bitParameters)
			sb.append("@"+s);
		sb.append("(");
		for(int i=0; i<parameters.size(); ++i) {
			if(i > 0) sb.append(", ");
			sb.append(parameters.get(i).left.name+" "+parameters.get(i).right);
		}
		sb.append(") {\n");
		sb.append(code.toString(2));
		sb.append("\n\t}\n");
		return sb.toString();
	}
}

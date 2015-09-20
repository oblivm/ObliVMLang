/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import java.util.HashMap;
import java.util.Map;

public class CanonicalName extends TypeVisitor<String> {

	private Map<String, String> canonicalMapping = new HashMap<String, String>();
	private int level = 1;
	
	private FunctionType type = null;
	
	@Override
	public String visit(ArrayType type) {
		return "ARRAY"+visit(type.type);
	}
	@Override
	public String visit(FloatType type) {
		return "FLOAT"+type.getLabel();
	}
	@Override
	public String visit(FunctionType type) {
		this.type = type;
		int s = 0;
		this.canonicalMapping.clear();
		for(Type t : type.typeParameters) {
			canonicalMapping.put(((VariableType)t).name, "T"+(++s));
		}
		StringBuffer sb = new StringBuffer();
		level = 2;
		sb.append(visit(type.returnType));
		for(int i=0; i<type.inputTypes.size(); ++i) {
			sb.append("_"+visit(type.inputTypes.get(i)));
		}
		return "FUNC_"+canonicalMapping.size()+"_"+sb.toString();
	}
	
	@Override
	public String visit(IntType type) {
		return "INT"+type.getLabel();
	}
	
	@Override
	public String visit(RecordType type) {
		StringBuffer sb = new StringBuffer();
		sb.append("RECORD");
		sb.append(type.name);
		for(int i=0; i<type.typeParameter.size(); ++i) {
			for(int j=0; j<level; ++j)
				sb.append("_");
			level ++;
			sb.append(visit(type.typeParameter.get(i)));
			level --;
		}
		return sb.toString();
	}
	
	@Override
	public String visit(RndType type) {
		return "RND"+type.getLabel();
	}
	
	@Override
	public String visit(VariableType type) {
		String name;
		if(this.canonicalMapping.containsKey(type.name)) {
			name = canonicalMapping.get(type.name);
		} else {
			name = "T"+(canonicalMapping.size() + 1);
			canonicalMapping.put(type.name, name);
			if(this.type != null) {
				this.type.typeParameters.add(new VariableType(type.name));
			}
		}
		return name;
	}
	@Override
	public String visit(VoidType type) {
		return "VOID";
	}
	@Override
	public String visit(NativeType type) {
		return "Native_"+type.name;
	}
	@Override
	public String visit(NullType type) {
		return "NullType";
	}
	@Override
	public String visit(DummyType type) {
		return "Nullable_"+visit(type.type);
	}
	

}

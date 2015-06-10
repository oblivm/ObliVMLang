/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oblivm.compiler.ast.type.ASTNativeType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.util.Pair;

public class TypeManager {
	private Map<String, Type> map = new HashMap<String, Type>();
	
	public Map<String, String> nativeNameMapping = new HashMap<String, String>();
	
	public Map<String, FunctionType> functions = new HashMap<String, FunctionType>();
	
	public List<Method> noClassFunctions = new ArrayList<Method>();
	
	public CanonicalName cn = new CanonicalName();
	
	public void add(FunctionType type, ASTType nativeType) {
		if(!(nativeType instanceof ASTNativeType))
			throw new RuntimeException("Not supported yet.");
		String name = cn.visit(type);
		functions.put(name, type);
		nativeNameMapping.put(name, ((ASTNativeType)nativeType).name);
	}
	
	public TypeManager() {
		Type noclass = new RecordType("NoClass", Label.Secure, new ArrayList<VariableConstant>());
		noclass.isStatic = true;
		
		map.put(noclass.name, noclass);
	}
	
	public void put(String name, Type type) {
		map.put(name, type);
	}
	
	public Type get(String name) {
		return map.get(name);
	}
	
	public void addMethod(Type base, Method method) {
		Type type;
		if(base == null) {
//			this.noClassFunctions.add(method);
			type = map.get("NoClass");
			type.addMethod(method);
//			FunctionType ty = method.getType();
//			String name = cn.visit(ty);
//			if(!functions.containsKey(name))
//				functions.put(name, ty);
		} else {
			type = get(base.name);
			type.addMethod(method);
		}
		
		for(Pair<Type, String> para : method.parameters) {
			if(para.left instanceof FunctionType) {
				String name = cn.visit(para.left);
				if(!functions.containsKey(name))
					functions.put(name, (FunctionType)para.left);
			}
		}
	}
	
	public List<Type> getTypes() {
		return new ArrayList<Type>(map.values());
	}
}

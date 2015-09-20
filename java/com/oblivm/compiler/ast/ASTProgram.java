/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.type.ASTFunctionType;
import com.oblivm.compiler.ast.type.ASTRecType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.util.Pair;

public class ASTProgram {
	public String packageName = "default";
	
	public List<Pair<String, ASTType>> typeDef 
		= new ArrayList<Pair<String, ASTType>>();
	public Map<String, List<ASTExpression>> typeBitVarDef 
	= new HashMap<String, List<ASTExpression>>();
	public Map<String, List<ASTType>> typeVarDef 
		= new HashMap<String, List<ASTType>>();
	public List<ASTFunction> functionDef 
		= new ArrayList<ASTFunction>();
	
	public List<Pair<ASTFunctionType, ASTType>> functionTypeMapping = new ArrayList<Pair<ASTFunctionType, ASTType>>();
	
	public ASTFunctionType getFunctionType(String name) {
		for(int i=0; i<functionDef.size(); ++i) {
			if(functionDef.get(i).name.equals(name)) {
				ASTFunction func = functionDef.get(i);
				return func.getType();
			}
		}
		return null;
	}
	
	public ASTType getBaseType(String name) {
		for(int i=0; i<functionDef.size(); ++i) {
			if(functionDef.get(i).name.equals(name)) {
				ASTFunction func = functionDef.get(i);
				return func.baseType;
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
//		for(Pair<String, ASTType> pair : typeDef) {
//			if((pair.right instanceof ASTRecType) && ((ASTRecType)pair.right).isDefine()) {
//				sb.append(pair.right.toString());
//			} else {
//				sb.append("typedef "+pair.left);
//				if(typeVarDef.get(pair.left) != null) {
//					sb.append("<");
//					List<ASTType> list = typeVarDef.get(pair.left);
//					for(int i=0; i<list.size(); ++i) {
//						if(i > 0) sb.append(", ");
//						sb.append(list.get(i).toString());
//					}
//					sb.append(">");
//				}
//				sb.append(" = " + pair.right);
//				sb.append(";\n");
//			}
//		}
		for(Pair<ASTFunctionType, ASTType> pair : functionTypeMapping) {
			sb.append("typedef "+pair.left+" = "+pair.right+";\n\n");
		}
		
		for(ASTFunction func : functionDef) {
			sb.append(func.toString(0));
		}
		return sb.toString();
	}
}

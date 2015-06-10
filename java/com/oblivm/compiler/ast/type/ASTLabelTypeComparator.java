/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import java.util.Map;

public class ASTLabelTypeComparator extends ASTDefaultTypeVisitor<Boolean> {
	private ASTLabelTypeComparator() {}
	
	private static ASTLabelTypeComparator inst = null;
	
	public static ASTLabelTypeComparator get() {
		if(inst == null) {
			inst = new ASTLabelTypeComparator();
		}
		return inst;
	}
	
	private ASTLabel label;
	
	public boolean compare(ASTLabel label, ASTType type) {
		this.label = label;
		return visit(type);
	}
	
	@Override
	public Boolean visit(ASTArrayType type) {
		return visit(type.type);
	}

	@Override
	public Boolean visit(ASTIntType type) {
		return label.less(type.getLabel());
	}

	@Override
	public Boolean visit(ASTFloatType type) {
		return label.less(type.getLabel());
	}

	@Override
	public Boolean visit(ASTRndType type) {
		return label.less(type.getLabel());
	}

	@Override
	public Boolean visit(ASTNativeType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTRecType type) {
		for(Map.Entry<String, ASTType> ent : type.fieldsType.entrySet()) {
			if(!visit(ent.getValue()))
				return false;
		}
		return true;
	}

	@Override
	public Boolean visit(ASTVariableType type) {
		// TODO variable type is by default secure; relax this notion
		return true;
	}

	@Override
	public Boolean visit(ASTVoidType type) {
		return true;
	}

	@Override
	public Boolean visit(ASTFunctionType type) {
		return true;
	}

	@Override
	public Boolean visit(ASTNullType type) {
		return true;
	}

	@Override
	public Boolean visit(ASTDummyType type) {
		// TODO dummy type is by default secure; need to change this later.
		return visit(type.type);
	}

	@Override
	public Boolean visit(ASTTupleType type) {
		for(ASTType ty : type.types)
			if(!visit(ty))
				return false;
		
		return true;
	}

}

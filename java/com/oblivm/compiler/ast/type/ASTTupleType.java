/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

public class ASTTupleType extends ASTType {
	public List<ASTType> types;
	
	public ASTTupleType(ASTType... types) {
		this.types = new ArrayList<ASTType>();
		for(ASTType ty : types)
			this.types.add(ty);
	}

	public ASTTupleType(List<ASTType> ret) {
		this.types = new ArrayList<ASTType>();
		this.types.addAll(ret);
	}

	@Override
	public boolean canFlowTo(ASTType type) {
		if(!(type instanceof ASTTupleType)) {
			return false;
		}
		ASTTupleType att = (ASTTupleType)type;
		if(types.size() != att.types.size())
			return false;
		for(int i=0; i<types.size(); ++i)
			if(!types.get(i).canFlowTo(att.types.get(i)))
				return false;
		return true;
	}

	@Override
	public String shortName() {
		StringBuffer sb = new StringBuffer();
		sb.append(types.get(0).shortName());
		for(int i=1; i<types.size(); ++i)
		{
			sb.append("*");
			sb.append(types.get(i));
		}
		return sb.toString();
	}

	@Override
	public ASTLabel getLabel() {
		ASTLabel lab = ASTLabel.Pub;
		for(int i=0; i<types.size(); ++i)
			lab = lab.meet(types.get(i).getLabel());
		return lab;
	}

	@Override
	public String toString(int indent) {
		return shortName();
	}
}

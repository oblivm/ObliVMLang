/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import com.oblivm.compiler.ir.BopExp.Op;

public class DummyType extends Type {

	public Type type;
	
	public DummyType(Type type) {
		super("dummy "+type.name);
		if(!type.constructable())
			throw new RuntimeException("Unconstructable type cannot be nullable.");
		this.type = type;
	}
	
	@Override
	public VariableConstant getBits() {
		VariableConstant bit = type.getBits();
		return bit instanceof Unknown ? bit : 
			new BOPVariableConstant(new Constant(1), Op.Add, bit);
	}

	@Override
	public Label getLabel() {
		return type.getLabel();
	}

	@Override
	public boolean constructable() {
		return true;
	}

}

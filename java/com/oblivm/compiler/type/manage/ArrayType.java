/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class ArrayType extends Type {
	public VariableConstant size;
	public Label2Infer indexLab;
	public Type type;
	
	public ArrayType(VariableConstant size, Label indexLab, Type type) {
		super(type.toString()+"["+size+"]");
		this.size = size;
		this.type = type;
		this.indexLab = new Label2Infer(indexLab);
	}

	@Override
	public VariableConstant getBits() {
		
//		if(this.indexLab.lab == Label.Pub) {
			VariableConstant vc = this.type.getBits();
			if(vc == null)
				return null;
//			return null;
			if(vc instanceof Unknown)
				return vc;
			else
				return new BOPVariableConstant(vc, com.oblivm.compiler.ir.BopExp.Op.Mul, size);
//		} else 
//			return null;
	}

	@Override
	public Label getLabel() {
		return type.getLabel().meet(indexLab.lab);
	}

	@Override
	public boolean constructable() {
		return type.constructable();
	}

	@Override
	public boolean writable() {
		return type.writable();
	}

	@Override
	public boolean similar(Type type) {
		if(type instanceof DummyType)
			return type.similar(this);
		if(!(type instanceof ArrayType))
			return false;
		ArrayType at = (ArrayType)type;
		return this.type.similar(at.type);
	}
	
}

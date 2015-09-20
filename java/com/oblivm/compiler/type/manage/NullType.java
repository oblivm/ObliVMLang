/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class NullType extends Type {

	public static NullType inst = new NullType();
	
	private NullType() {
		super("null");
	}
	
	@Override
	public VariableConstant getBits() {
		return new Constant(1);
	}

	@Override
	public Label getLabel() {
		return Label.Pub;
	}

	@Override
	public boolean constructable() {
		return true;
	}

	@Override
	public boolean writable() {
		return true;
	}
	
	@Override
	public boolean similar(Type type) {
		return (type instanceof DummyType) || (type instanceof NullType);
	}

}

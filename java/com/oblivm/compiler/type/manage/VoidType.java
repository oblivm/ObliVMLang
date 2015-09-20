/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class VoidType extends Type {
	
	private static VoidType inst = null;
	
	public static VoidType get() {
		if(inst == null)
			inst = new VoidType();
		return inst;
	}
	
	private VoidType() {
		super("void");
	}
	
	public String toString(int indent) {
		return toString();
	}

	public String toString() {
		return "void";
	}

	@Override
	public VariableConstant getBits() {
		return new Constant(0);
	}

	@Override
	public Label getLabel() {
		return Label.Pub;
	}

	@Override
	public boolean constructable() {
		return false;
	}

	@Override
	public boolean writable() {
		return false;
	}
	
	@Override
	public boolean similar(Type type) {
		return type instanceof VoidType;
	}
}

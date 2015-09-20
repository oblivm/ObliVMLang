/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class FloatType extends Type {

	public VariableConstant bit;
	private Label2Infer lab; 
	
	public void setLabel(Label lab) {
		this.lab.lab = lab;
	}
	
	public FloatType(VariableConstant bit, Label lab) {
		super(bit == null ? "float" : "float@"+bit);
		this.bit = bit;
		this.lab = new Label2Infer(lab);
	}

	
	public boolean equals(Object obj) {
		if(!(obj instanceof FloatType))
			return false;
		FloatType other = (FloatType)obj;
		return bit.equals(other.bit) && lab.lab == other.lab.lab;
	}
	
	public boolean rawType() {
		return true;
	}
	
	@Override
	public VariableConstant getBits() {
		return bit;
	}

	@Override
	public Label getLabel() {
		return lab.lab;
	}
	
	public String toString() {
		return this.lab.lab + " " + this.name;
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
		if(type instanceof DummyType)
			return type.similar(this);
		if(!(type instanceof FloatType))
			return false;
		return true;
	}
}

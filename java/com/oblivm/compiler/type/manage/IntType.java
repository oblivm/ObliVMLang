/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class IntType extends Type {

	public VariableConstant bit;
	private Label2Infer lab; 
	
	public void setLabel(Label lab) {
		this.lab.lab = lab;
	}
	
	public IntType(int bit, Label lab) {
		this(new Constant(bit), lab);
	}
	
	public IntType(VariableConstant bit, Label lab) {
		super(bit == null ? "int" : "int@"+bit);
		this.bit = bit;
		this.lab = new Label2Infer(lab);
	}

	
	public boolean equals(Object obj) {
		if(!(obj instanceof IntType))
			return false;
		IntType other = (IntType)obj;
		return bit.equals(other.bit) && lab.lab == other.lab.lab;
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
	
	public boolean rawType() {
		return true;
	}
}

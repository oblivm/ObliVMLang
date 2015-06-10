/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class RndType extends Type {

	public VariableConstant bit;
	private Label2Infer lab; 
	
	public void setLabel(Label lab) {
		this.lab.lab = lab;
	}
	
	public RndType(VariableConstant bit, Label lab) {
		super(bit == null ? "rnd" : "rnd@"+bit);
		this.bit = bit;
		this.lab = new Label2Infer(lab);
	}

	
	public boolean equals(Object obj) {
		if(!(obj instanceof RndType))
			return false;
		RndType other = (RndType)obj;
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
}

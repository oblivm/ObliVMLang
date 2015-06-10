package com.oblivm.compiler.ir;

public class UsingBlock extends IRCode {
	public IRCode code;

	public UsingBlock(IRCode code) {
		this.code = code;
	}
	
	@Override
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.indent(indent)+"{");
		sb.append(code.toString(indent+1));
		sb.append(this.indent(indent)+"}");
		return sb.toString();
	}

	@Override
	public IRCode clone(boolean withTypeDef) {
		return new UsingBlock(code.clone(withTypeDef));
	}
}

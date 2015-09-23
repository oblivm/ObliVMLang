package com.oblivm.compiler.ir;

import com.oblivm.compiler.ast.Position;

public class Debug extends IRCode {
	public IRCode code;
	public Variable toShow;
	public Position position;
	public Variable cond;
	
	public Debug(IRCode code, Variable toShow, Variable cond, Position pos) {
		this.code = code;
		this.toShow = toShow;
		this.position = pos;
		this.cond = cond;
	}
	
	@Override
	public String toString(int indent) {
		return indent(indent) + "Debug {\n"
					+ code.toString(indent+1)+"\n"
					+ indent(indent+1)+"at "+position+" debug("+toShow+")\n" 
					+ indent(indent) + "}\n";
	}
	@Override
	public IRCode clone(boolean withTypeDef) {
		IRCode ret = new Debug(code.clone(withTypeDef), toShow, cond, position);
		ret.withTypeDef = withTypeDef;
		return ret;
	}
}

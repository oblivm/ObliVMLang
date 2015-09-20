package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.Type;

public class SizeofExp extends Expression {
	public Type type;
	
	public SizeofExp(Type type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "sizeof("+this.type+")";
	}

	@Override
	public Label getLabels() {
		return Label.Pub;
	}


}

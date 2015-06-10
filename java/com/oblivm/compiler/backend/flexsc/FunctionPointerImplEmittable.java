/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend.flexsc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.oblivm.compiler.type.manage.FunctionType;
import com.oblivm.compiler.type.manage.Method;
import com.oblivm.compiler.type.manage.Type;
import com.oblivm.compiler.type.manage.TypeManager;
import com.oblivm.compiler.type.manage.VariableType;
import com.oblivm.compiler.util.Pair;

public class FunctionPointerImplEmittable extends Emittable {
	CodeGenVisitor codeGen;
	TypeManager tm;
	String name;
	FunctionType fty;
	Method meth;
	
	public FunctionPointerImplEmittable(Config config, CodeGenVisitor codeGen, 
			Method meth, TypeManager tm) throws IOException {
		super(new PrintStream(new File(config.path+"/"+tm.cn.visit(meth.getType())+"Impl.java")), config, tm.cn.visit(meth.getType())+"Impl");
		this.fty = meth.getType();		
		this.codeGen = codeGen;
		this.name = tm.cn.visit(fty);
		this.tm = tm;
		this.meth = meth;
		
		for(int i = 0; i<fty.typeParameters.size(); ++i) {
			this.typeParameters.add(fty.typeParameters.get(i)+" extends IWritable<"+fty.typeParameters.get(i)+","+codeGen.dataType+">");
		}

		this.superClass = this.name;
		if(fty.typeParameters.size() > 0) {
			this.superClass += "<";
			for(int i=0; i<fty.typeParameters.size(); ++i) {
				if(i > 0)
					this.superClass += ", ";
				this.superClass += fty.typeParameters.get(i);
			}
			this.superClass += ">";
		}
	}
	
	@Override
	public void emitFieldsInternal() {
		// Inherit from super class
	}

	@Override
	public void emitConstructorInternal() {
		out.print("\tpublic ");
		out.print(className+"(");
//		out.print("CompEnv<"+codeGen.dataType+"> env, IntegerLib<"+codeGen.dataType+"> intLib");
		out.print("CompEnv<"+codeGen.dataType+"> env");
		for(int i=0; i<fty.typeParameters.size(); ++i) {
			out.print(", ");
			out.print(fty.typeParameters.get(i)+" factory"+fty.typeParameters.get(i));
		}
		out.println(") throws Exception {");
		out.print("\t\tsuper(env");
		for(Type tt : fty.typeParameters) {
			out.print(", factory"+((VariableType)tt).name);
		}
		out.println(");\n\t}");

		out.println();
	}

	public void emitCalc() {
		out.print("\tpublic "+codeGen.visit(fty.returnType)+" calc(");
		for(int i=0; i<fty.inputTypes.size(); ++i) {
			if(i > 0) out.print(", ");
			out.print(codeGen.visit(fty.inputTypes.get(i))+" "+this.meth.parameters.get(i).right);
		}
		out.println(") throws Exception {");
		for(Pair<Type, String> ent : meth.localVariables) {
			String cons = codeGen.constructor(ent.left);
			if(cons != null)
				cons = " = "+cons;
			else
				cons = "";
			out.println("\t\t"+codeGen.visit(ent.left)+" "+ent.right+cons+";");
		}
		codeGen.indent = 2;
		out.println(codeGen.visit(meth.code));
		out.println("\t}");
	}
		
	@Override
	public void emitMethodsInternal() {
		this.emitCalc();
	}

}

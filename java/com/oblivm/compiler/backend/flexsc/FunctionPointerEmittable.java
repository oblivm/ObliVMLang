/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend.flexsc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.oblivm.compiler.type.manage.FunctionType;
import com.oblivm.compiler.type.manage.TypeManager;

public class FunctionPointerEmittable extends Emittable {
	CodeGenVisitor codeGen;
	TypeManager tm;
	String name;
	FunctionType fty;
	
	public FunctionPointerEmittable(Config config, CodeGenVisitor codeGen, 
			String name, TypeManager tm) throws IOException {
		super(new PrintStream(new File(config.path+"/"+name+".java")), config, name);
		this.codeGen = codeGen;
		this.name = name;
		this.tm = tm;
		this.fty = tm.functions.get(name);
		this.isAbstract = true;

		if(Config.useTemplate) 
			this.typeParameters.add(codeGen.dataType);
		
		for(int i = 0; i<fty.typeParameters.size(); ++i) {
			this.typeParameters.add(fty.typeParameters.get(i)+" extends IWritable<"+fty.typeParameters.get(i)+","+codeGen.dataType+">");
		}
		
		if(tm.nativeNameMapping.containsKey(name)) {
			this.implementInterfaces.add(tm.nativeNameMapping.get(name)+"<"+codeGen.dataType+">");
		}
	}
	
	@Override
	public void emitFieldsInternal() {
		// Environment
//		out.println("\tCompEnv<"+codeGen.dataType+"> env;");
//		out.println("\tIntegerLib<"+codeGen.dataType+"> intLib;");
//		out.println("\tFloatLib<"+codeGen.dataType+"> floatLib;");
//
//		for(Type tt : fty.typeParameters) {
//			out.println("\t"+tt+" factory"+((VariableType)tt).name+";");
//		}
//
//		out.println();
	}

	@Override
	public void emitConstructorInternal() {
//		out.print("\tpublic ");
//		out.print(name+"(");
////		out.print("CompEnv<"+codeGen.dataType+"> env, IntegerLib<"+codeGen.dataType+"> intLib");
//		out.print("CompEnv<"+codeGen.dataType+"> env");
//		for(int i=0; i<fty.typeParameters.size(); ++i) {
//			out.print(", ");
//			out.print(fty.typeParameters.get(i)+" factory"+fty.typeParameters.get(i));
//		}
//		out.println(") throws Exception {");
//		out.println("\t\tthis.env = env;");
//		// Hard Code Here!
//		out.println("\t\tthis.intLib = new IntegerLib<"+codeGen.dataType+">(env);");
//		out.println("\t\tthis.floatLib = new FloatLib<"+codeGen.dataType+">(env, 24, 8);");
//		// Hard Code Finished!
//		for(Type tt : fty.typeParameters) {
//			String name = ((VariableType)tt).name;
//			out.println("\t\tthis.factory"+name+" = factory"+name+";");
//		}
//		out.println("\t}");
//
//		out.println();
	}

	public void emitCalc() {
		out.print("\tpublic "+codeGen.visit(fty.returnType)+" calc(");
		for(int i=0; i<fty.inputTypes.size(); ++i) {
			if(i > 0) out.print(", ");
			out.print(codeGen.visit(fty.inputTypes.get(i))+" x"+i);
		}
		out.println(") throws Exception;");
	}
	
	public void emitNativeMethod() {
		out.print("\tpublic "+codeGen.nativeName(fty.returnType)+" "+fty.name+"(");
		for(int i=0; i<fty.inputTypes.size(); ++i) {
			if(i > 0) out.print(", ");
			out.print(codeGen.nativeName(fty.inputTypes.get(i))+" x"+i);
		}
		out.println(") throws Exception {");
		StringBuffer sb = new StringBuffer();
		sb.append("calc(");
		for(int i=0; i<fty.inputTypes.size(); ++i) {
			if(i > 0)
				sb.append(", ");
			String calcType = codeGen.visit(fty.inputTypes.get(i));
			String nativeType = codeGen.nativeName(fty.inputTypes.get(i));
			if(calcType.equals(nativeType)) {
				sb.append("x"+i);
			} else {
				sb.append(codeGen.constructor(fty.inputTypes.get(i))+".newObj(x"+i+")");
			}
		}
		sb.append(")");
		String nn = codeGen.nativeName(fty.returnType);
		if(!codeGen.visit(fty.returnType).equals(nn)) {
			sb.append(".getBits()");
			if(!nn.endsWith("[]"))
				sb.append("[0]");
		}
		out.println("\t\treturn "+sb.toString()+";");
		out.println("\t}");
	}
	
	@Override
	public void emitMethodsInternal() {
		this.emitCalc();
		
		if(tm.nativeNameMapping.containsKey(name))
			emitNativeMethod();
	}

}

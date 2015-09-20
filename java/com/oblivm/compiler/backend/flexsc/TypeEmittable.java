/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend.flexsc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import com.oblivm.compiler.ir.BopExp.Op;
import com.oblivm.compiler.type.manage.ArrayType;
import com.oblivm.compiler.type.manage.BOPVariableConstant;
import com.oblivm.compiler.type.manage.BitVariable;
import com.oblivm.compiler.type.manage.Constant;
import com.oblivm.compiler.type.manage.FloatType;
import com.oblivm.compiler.type.manage.IntType;
import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.Method;
import com.oblivm.compiler.type.manage.NativeType;
import com.oblivm.compiler.type.manage.RecordType;
import com.oblivm.compiler.type.manage.RndType;
import com.oblivm.compiler.type.manage.Type;
import com.oblivm.compiler.type.manage.Unknown;
import com.oblivm.compiler.type.manage.VariableConstant;
import com.oblivm.compiler.type.manage.VariableType;
import com.oblivm.compiler.util.Pair;

public class TypeEmittable extends Emittable {
	CodeGenVisitor codeGen;
	RecordType type;

	Method main = null;

	public TypeEmittable(Config config, CodeGenVisitor codeGen, RecordType type) throws IOException {
		super(new PrintStream(new File(config.path+"/"+type.name+".java")), config, type.name);
		this.codeGen = codeGen;
		this.type = type;

		if(type.typeParameter != null) {
			for(int i = 0; i<type.typeParameter.size(); ++i) {
				this.typeParameters.add(
						((VariableType)type.typeParameter.get(i)).name
						+" extends IWritable<"+((VariableType)type.typeParameter.get(i)).name+","+codeGen.dataType+">"
						);
			}
		}

		codeGen.isDefine = false;
		if(this.inheritWritable(type)) {
			this.implementInterfaces.add("IWritable<"+codeGen.visit(type)+", "+codeGen.dataType+">");
		}
		type.getMethods().contains("main");
		main = null;
		if(type.name.equals("NoClass")) {
			for(Method meth : type.getMethods()) {
				if(meth.name.equals("main")) {
					this.implementInterfaces.add("ISecureRunnable<"+codeGen.dataType+">");
					if ( main != null)
						throw new RuntimeException("Multiple main function!");
					main = meth;
				}
			}
		}
	}


	public void emitClassHeader() {
		if(isClosed)
			throw new RuntimeException("Output stream is closed!");
		if(isAbstract)
			out.print("abstract ");
		out.print("public class "+className);
		if(Config.useTemplate) {
			if(typeParameters.size() > 0) {
				out.print("<");
				out.print(codeGen.dataType);
				for(int i=0; i<typeParameters.size(); ++i) {
					out.print(", ");
					out.print(typeParameters.get(i));
				}
				out.print(">");
			}  else {
				out.print("<"+codeGen.dataType+">");
			}
		} else {
			if(typeParameters.size() > 0) {
				out.print("<");
				for(int i=0; i<typeParameters.size(); ++i) {
					if(i > 0) out.print(", ");
					out.print(typeParameters.get(i));
				}
				out.print(">");
			}
		}
		if(superClass != null)
			out.print(" extends "+superClass);
		if(implementInterfaces.size() > 0) {
			out.print(" implements ");
			for(int i=0; i<implementInterfaces.size(); ++i) {
				if(i > 0)
					out.print(", ");
				out.print(implementInterfaces.get(i));
			}
		}
		out.println(" {");
	}


	@Override
	public void emitFieldsInternal() {
		// Local variables
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			out.println("\tpublic "+codeGen.visit(ent.getValue())+" "+ent.getKey()+";");
		}
		out.println();

		// Environment
		if(type.name.equals("NoClass")) {
			for(int i=0; i<Config.types.length; ++i)
				out.println("\tpublic "+Config.types[i].replace("@dataType", codeGen.dataType)+" "+Config.fields[i]+";");
			out.println("\tpublic NoClass<" + codeGen.dataType+"> noclass = this;");
		} else {
			for(int i=0; i<Config.types.length; ++i)
				out.println("\tpublic "+Config.types[i].replace("@dataType", codeGen.dataType)+" "+Config.fields[i]+";");
		}

		if(!type.name.equals("NoClass")) {
			out.println("\tprivate NoClass"+(Config.useTemplate ? "<"+codeGen.dataType+">" : "")+" noclass;");
		}

		// Factories
		for(Type tt : type.typeParameter) {
			VariableType vt = (VariableType)tt;
			out.println("\tprivate "+vt.name+" factory"+vt.name+";");
		}

		// Bit variables
		for(VariableConstant vc : type.bits) {
			BitVariable bit = (BitVariable)vc;
			out.println("\tprivate int "+bit.var+";");
		}
		out.println();
	}

	@Override
	public void emitConstructorInternal() {
		if(!type.constructable())
			return;

		// Constructor
		out.println("\tpublic "+codeGen.constructorWithNonStruct(type)+" throws Exception {");

		for(int i=0; i<Config.fields.length; ++i) {
			out.println("\t\tthis."+Config.fields[i]+" = "+Config.fields[i]+";");
			break;
		}
		// Hard Code Here!
		out.println("\t\tthis.intLib = new IntegerLib<"+codeGen.dataType+">(env);");
		out.println("\t\tthis.floatLib = new FloatLib<"+codeGen.dataType+">(env, 24, 8);");
		if(!type.name.equals("NoClass")) {
			out.println("\t\tthis.noclass = new NoClass"+(Config.useTemplate ? "<"+codeGen.dataType+">" : "") + "(env);");
		}
		// Hard Code Finished!

		for(VariableConstant vc : type.bits) {
			BitVariable bit = (BitVariable)vc;
			out.println("\t\tthis."+bit.var+" = "+bit.var+";");
		}
		if(type.typeParameter != null)
			for(Type tt : type.typeParameter) {
				VariableType vt = (VariableType)tt;
				out.println("\t\tthis.factory"+vt.name+" = factory"+vt.name+";");
			}
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			String cons = codeGen.constructor(ent.getValue());
			if(ent.getValue() instanceof NativeType) {
				out.println("\t\tthis."+ent.getKey()+" = "+cons+";");
			} else if(ent.getValue().getBits() == null) {
				// TODO this doesn't look correct.
				out.println("\t\tthis."+ent.getKey()+" = "+ent.getKey()+";");
			} else {
				if(cons == null)
					continue;
				out.println("\t\tthis."+ent.getKey()+" = "+cons+";");
				if(ent.getValue() instanceof ArrayType) {
					ArrayType at = (ArrayType)ent.getValue();
					if(at.indexLab.lab != Label.Pub)
						continue;
					ArrayList<VariableConstant> list = new ArrayList<VariableConstant>();
					list.add(at.size);
					Type ty = at.type;
					while(ty instanceof ArrayType) {
						at = (ArrayType)ty;
						if(at.indexLab.lab != Label.Pub)
							break;
						list.add(at.size);
						ty = at.type;
					}
					String tab = "\t\t";
					cons = codeGen.constructor(ty);
					for(int i=0; i<list.size(); ++i) {
						String var = "__haha_i"+i;
						out.println(tab+"for(int "+var+"=0; "+var+" < " + list.get(i).toString() + "; ++"+var+") {");
						tab += "\t";
					}
					out.print(tab+"this."+ent.getKey());
					for(int i=0; i<list.size(); ++i) {
						String var = "__haha_i"+i;
						out.print("["+var+"]");
					}
					out.println(" = "+cons+";");
					tab = tab.substring(0, tab.length() - 1);
					for(int i=0; i<list.size(); ++i) {
						out.println(tab + "}");
						tab = tab.substring(0, tab.length() - 1);
					}
				}
			}
		}
		out.println("\t}");

		out.println();
	}

	public boolean inheritWritable(Type type) {
		return type.writable();
		//		return type.getBits() != null;
	}

	public void emitNumBits() {
		out.println("\tpublic int numBits() {");
		if(!(type.getBits() instanceof Unknown)) {
			out.println("\t\treturn "+type.getBits()+";\n\t}\n");
		} else {
			out.println("\t\tint sum = 0;");
			for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
				if(ent.getValue().getLabel() == Label.Pub)
					continue;
				if(ent.getValue() instanceof VariableType) {
					VariableType vt = (VariableType)ent.getValue();
					out.println("\t\tsum += factory"+vt.name+".numBits();");
				} else if (ent.getValue() instanceof IntType ||
						ent.getValue() instanceof RndType ||
						ent.getValue() instanceof FloatType
						) {
					out.println("\t\tsum += "+ent.getKey()+".length;");
				} else if (ent.getValue() instanceof ArrayType) {
					Type type = ent.getValue();
					VariableConstant vc = new Constant(1);
					String suffix = "";
					while(type instanceof ArrayType) {
						ArrayType at = (ArrayType) type;
						vc = new BOPVariableConstant(vc, Op.Mul, at.size);
						if(((ArrayType)type).indexLab.lab == Label.Secure)
							break;
						type = at.type;
						suffix += "[0]";
					}
					out.println("\t\tsum += "+ent.getKey()+".length == 0 ? 0 : "+ent.getKey()+suffix
						+ (type instanceof ArrayType ? ".dataSize" : type.rawType() ? ".length" : ".numBits()")
						+ "*" + vc.toString() + ";");
				} else {
					out.println("\t\tsum += "+ent.getKey()+".numBits();");
				}
			}
			out.println("\t\treturn sum;");
			out.println("\t}\n");
		}
	}

	public void emitGetBits() {
		out.println("\tpublic "+codeGen.dataType+"[] getBits() throws Exception {");
		//		out.println("\t\t"+codeGen.dataType+"[] ret = new "+codeGen.dataType+"[this.numBits()];");
		out.println("\t\t"+codeGen.dataType+"[] ret = env.newTArray(this.numBits());");
		out.println("\t\t"+codeGen.dataType+"[] tmp_b;");
		out.println("\t\t"+codeGen.dataType+" tmp;");
		out.println("\t\tint now = 0;");
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			if(ent.getValue().getLabel() == Label.Pub)
				continue;
			String var = "tmp_b";
			if(ent.getValue().getBits() != null && ent.getValue().getBits().isConstant(1))
				var = "tmp";
			if(ent.getValue().rawType())
				out.println("\t\t"+var+" = "+ent.getKey()+";");
			else if (ent.getValue() instanceof ArrayType) {
				// TODO handling multi-dimensional ORAM might be wrong
				ArrayType at = (ArrayType) ent.getValue();
				VariableConstant vc = at.size;
				String template = "%x[i0]";
				if (at.indexLab.lab == Label.Secure) {
					template = "%x.read(intLib.toSignals(i0))";
				}
				String prefix = "\t\t";
				int now = 0;
				out.println(prefix+"for(int %i=0; %i<%vc; ++%i)".replaceAll("%i", "i"+now).replaceAll("%vc", vc.toString()));
				while(at.type instanceof ArrayType) {
					now ++;
					prefix += "\t";
					at = (ArrayType)at.type;
					vc = at.size;
					out.println(prefix+"for(int %i=0; %i<%vc; ++%i)".replaceAll("%i", "i"+now).replaceAll("%vc", vc.toString()));
					String tmplt = "%x[%i]";
					if(at.indexLab.lab == Label.Secure) {
						tmplt = "%x.read(intLib.toSignal(%i))";
					}
					template = tmplt.replaceAll("%i", "i"+now).replaceAll("%x", template);
				}
				out.println(prefix + "{");
				out.println(prefix + "\ttmp_b = "+template.replaceAll("%x", ent.getKey())+(!(at.type instanceof RecordType) ? ";" : ".getBits();"));
				out.println(prefix + "\tSystem.arraycopy(tmp_b, 0, ret, now, tmp_b.length);");
				out.println(prefix + "\tnow += tmp_b.length;");
				out.println(prefix + "}");
			} else {
				out.print("\t\t"+var+" = this."+ent.getKey()+".getBits()");
				if(var.equals("tmp"))
					out.print("[0]");
				out.println(";");
			}
			if(!(ent.getValue() instanceof ArrayType)) {
				if(var.equals("tmp")) {
					out.println("\t\tret[now] = tmp;");
					out.println("\t\tnow ++;");
				} else {
					out.println("\t\tSystem.arraycopy(tmp_b, 0, ret, now, tmp_b.length);");
					out.println("\t\tnow += tmp_b.length;");
				}
			}
		}
		out.println("\t\treturn ret;");
		out.println("\t}\n");
	}

	public void emitNewObj() {
		out.println("\tpublic "+codeGen.visit(type)+" newObj("+codeGen.dataType+"[] data) throws Exception {");
		out.println("\t\tif(data == null) {");
		//		out.println("\t\t\tdata = new "+codeGen.dataType+"[this.numBits()];");
		out.println("\t\t\tdata = env.newTArray(this.numBits());");
		out.println("\t\t\tfor(int i=0; i<this.numBits(); ++i) { data[i] = intLib.SIGNAL_ZERO; }");
		out.println("\t\t}");
		out.println("\t\tif(data.length != this.numBits()) return null;");
		out.println("\t\t"+codeGen.visit(type)+" ret = new "+codeGen.constructor(type, false)+";");
		out.println("\t\t"+codeGen.dataType+"[] tmp;");
		out.println("\t\tint now = 0;");
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			if(ent.getValue().getLabel() == Label.Pub)
				continue;
			if(ent.getValue() instanceof IntType) {
				IntType it = (IntType)ent.getValue();
				if(it.bit.isConstant(1)) {
					out.println("\t\tret."+ent.getKey()+" = data[now];");
					out.println("\t\tnow ++;");
				} else {
					out.println("\t\tret."+ent.getKey()+" = env.newTArray("+it.bit+");");
					out.println("\t\tSystem.arraycopy(data, now, ret."+ent.getKey()+", 0, "+it.bit+");");
					out.println("\t\tnow += "+it.bit+";");
				}
			} else if(ent.getValue() instanceof RndType) {
				RndType it = (RndType)ent.getValue();
				if(it.bit.isConstant(1)) {
					out.println("\t\tret."+ent.getKey()+" = data[now];");
					out.println("\t\tnow ++;");
				} else {
					out.println("\t\tret."+ent.getKey()+" = env.newTArray("+it.bit+");");
					out.println("\t\tSystem.arraycopy(data, now, ret."+ent.getKey()+", 0, "+it.bit+");");
					out.println("\t\tnow += "+it.bit+";");
				}
			} else if(ent.getValue() instanceof FloatType) {
				FloatType it = (FloatType)ent.getValue();
				if(it.bit.isConstant(1)) {
					out.println("\t\tret."+ent.getKey()+" = data[now];");
					out.println("\t\tnow ++;");
				} else {
					out.println("\t\tret."+ent.getKey()+" = env.newTArray("+it.bit+");");
					out.println("\t\tSystem.arraycopy(data, now, ret."+ent.getKey()+", 0, "+it.bit+");");
					out.println("\t\tnow += "+it.bit+";");
				}
			} else if (ent.getValue() instanceof RecordType){
				RecordType rt = (RecordType)ent.getValue();
				out.println("\t\tret."+ent.getKey()+" = new "+codeGen.constructor(rt, false)+";");
				out.println("\t\ttmp = env.newTArray(this."+ent.getKey()+".numBits());");
				out.println("\t\tSystem.arraycopy(data, now, tmp, 0, tmp.length);");
				out.println("\t\tnow += tmp.length;");
				out.println("\t\tret."+ent.getKey()+" = ret."+ent.getKey()+".newObj(tmp);");
			} else if (ent.getValue() instanceof ArrayType) {
				ArrayType at = (ArrayType)ent.getValue();
				//				out.println("\t\ttmp = env.newTArray(this.factory"+vt.name+".numBits());");
				//				out.println("\t\tSystem.arraycopy(data, now, tmp, 0, tmp.length);");
				//				out.println("\t\tnow += tmp.length;");
				//				out.println("\t\tret."+ent.getKey()+" = ret.factory"+vt.name+".newObj(tmp);");
			} else if (ent.getValue() instanceof VariableType) {
				VariableType vt = (VariableType)ent.getValue();
				out.println("\t\ttmp = env.newTArray(this.factory"+vt.name+".numBits());");
				out.println("\t\tSystem.arraycopy(data, now, tmp, 0, tmp.length);");
				out.println("\t\tnow += tmp.length;");
				out.println("\t\tret."+ent.getKey()+" = ret.factory"+vt.name+".newObj(tmp);");
			} else if (ent.getValue() instanceof NativeType) {
				NativeType nt = (NativeType)ent.getValue();
				//TODO
			} else
				throw new RuntimeException("Unconstructable type!");
		}
		out.println("\t\treturn ret;");
		out.println("\t}\n");
	}

	public void emitFake() {
		out.println("\tpublic "+codeGen.visit(type)+" fake() throws Exception {");
		out.println("\t\t"+codeGen.visit(type)+" ret = new "+codeGen.constructor(type, false)+";");
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			if(ent.getValue() instanceof IntType) {
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+";");
			} else if(ent.getValue() instanceof RndType) {
				out.println("\t\tret."+ent.getKey()+" = intLib.randBools(this."+ent.getKey()+".length);");
			} else if(ent.getValue() instanceof FloatType) {
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+";");
			} else if (ent.getValue() instanceof RecordType){
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+".fake();");
			} else if (ent.getValue() instanceof ArrayType) {
				ArrayType at = (ArrayType)ent.getValue();
			} else {
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+".fake();");
			}
		}
		out.println("\t\treturn ret;");
		out.println("\t}\n");
	}

	public void emitMuxFake() {
		out.println("\tpublic "+codeGen.visit(type)+" muxFake("+codeGen.dataType+" __isDummy) throws Exception {");
		out.println("\t\t"+codeGen.visit(type)+" ret = new "+codeGen.constructor(type, false)+";");
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			if(ent.getValue().getLabel() == Label.Pub) {
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+";");
				continue;
			}
			if(ent.getValue() instanceof IntType) {
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+";");
			} else if(ent.getValue() instanceof RndType) {
				out.println("\t\tret."+ent.getKey()+" = intLib.mux(this."+ent.getKey()+", intLib.randBools(this."+ent.getKey()+".length), __isDummy);");
			} else if(ent.getValue() instanceof FloatType) {
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+";");
			} else if (ent.getValue() instanceof RecordType){
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+".muxFake(__isDummy);");
			} else if (ent.getValue() instanceof ArrayType) {
				ArrayType at = (ArrayType)ent.getValue();
			} else {
				out.println("\t\tret."+ent.getKey()+" = this."+ent.getKey()+".muxFake(__isDummy);");
			}
		}
		out.println("\t\treturn ret;");
		out.println("\t}\n");	}

	public void emitWritableFunctions() {
		this.emitNumBits();
		this.emitGetBits();
		this.emitNewObj();
		this.emitFake();
		this.emitMuxFake();
	}

	public void emitAMethod(Method method) {
		out.print("\tpublic ");
		out.print(codeGen.visit(method.returnType)+" "+method.name+"(");
		boolean f = true;
		for(String s : method.bitParameters) {
			if(f) f = false;
			else out.print(", ");
			out.print("int "+s);
		}
		for(Pair<Type, String> ent : method.parameters) {
			if(f) f = false;
			else out.print(", ");
			out.print(codeGen.visit(ent.left)+" "+ent.right);
		}
		if(method.isPhantom) {
			if(f) f = false;
			else out.print(", ");
			out.print(codeGen.dataType+" __isPhantom");
		}
		out.println(") throws Exception {");
		if(method.isPhantom) {
			// Phantomize the input
			for(Pair<Type, String> ent : method.parameters) {
				Type type = ent.left;
				if(type.getLabel() == Label.Pub)
					continue;
				if(type instanceof RndType) {
					out.println("\t\t"+ent.right+" = intLib.mux("
							+ ent.right+", intLib.randBools("+ent.right+".length), __isPhantom);");
				} else if(type instanceof RecordType || type instanceof VariableType) {
					out.println("\t\t"+ent.right+" = "+ent.right+".muxFake(__isPhantom);");
				}
			}
		}

		for(Pair<Type, String> ent : method.localVariables) {
			String cons = codeGen.constructor(ent.left);
			if(cons != null)
				cons = " = "+cons;
			else
				cons = "";
			out.println("\t\t"+codeGen.visit(ent.left)+" "+ent.right+cons+";");
			initialize(ent.right, ent.left, 2);
		}
		codeGen.indent = 2;
		out.println(codeGen.visit(method.code));
		out.println("\t}");
	}

	private String indent(int lvl) {
		if(lvl == 0)
			return "";
		else
			return "\t"+indent(lvl-1);
	}

	private void initialize(String field, Type type, int level) {
		if(type instanceof ArrayType) {
			ArrayType at = (ArrayType)type;
			String cons = codeGen.constructor(at.type);
			String li = "_j_"+level;
			if(cons != null) {
				out.println(indent(level)+"for(int "+li+" = 0; "+li+" < "+at.size+"; ++"+li+") {");
				String next = at.indexLab.lab == Label.Secure 
						? field+".read(intLib.toSignals("+li+"))" 
								: field+"["+li+"]";
				if(at.indexLab.lab == Label.Secure) {
					out.println(indent(level+1)+field+".write(intLib.toSignals("+li+"), "+cons+");");
				} else {
					out.println(indent(level+1)+next+" = "+cons+";");
				}
				initialize(next, 
						at.type, level + 1);
				out.println(indent(level)+"}");
			}
		} else if(type instanceof RecordType) {
			RecordType rt = (RecordType)type;
			for(Map.Entry<String, Type> ent : rt.fields.entrySet()) {
				initialize(field+"."+ent.getKey(), ent.getValue(), level);
			}
		}
	}

	// used by emitMain only
	private String getIntParameter(Type type) {
		String noneRet = "<null>";
		if(!(type instanceof IntType))
			return noneRet;
		IntType it = (IntType)(type);
		if(it.getBits() instanceof BitVariable) {
			return ((BitVariable)it.getBits()).var;
		} else
			return noneRet;
	}

	// used by emitMain only
	private String getArraySizeParameter(Type type) {
		String noneRet = "<null>";
		if(!(type instanceof ArrayType))
			return noneRet;
		ArrayType at = (ArrayType)type;
		if(!(at.type instanceof IntType))
			return noneRet;
		IntType it = (IntType)(at.type);
		if((at.size instanceof BitVariable) && (it.bit instanceof Constant)) {
			return ((BitVariable)at.size).var;
		} else
			return noneRet;
	}

	// used by emitMain only
	private String getArrayIntSizeParameter(Type type) {
		String noneRet = "<null>";
		if(!(type instanceof ArrayType))
			return noneRet;
		ArrayType at = (ArrayType)type;
		if(!(at.type instanceof IntType))
			return noneRet;
		IntType it = (IntType)(at.type);
		if((it.bit instanceof BitVariable) && (at.size instanceof Constant)) {
			return ((BitVariable)it.bit).var;
		} else
			return noneRet;
	}

	private void emitMain() {
		if (main.bitParameters.size() == 2 
				&& main.parameters.size() == 2
				&& main.parameters.get(0).left instanceof IntType
				&& main.parameters.get(1).left instanceof IntType) {
			return;
		}

		if (!(main.returnType instanceof IntType)) {
			throw new RuntimeException("Unsupported main function's return type: only support return int.");
		}

		if (main.parameters.size() != 2) {
			throw new RuntimeException("main functions only take two inputs: alice and bob.");
		}
		out.print("\tpublic ");
		out.print(codeGen.visit(main.returnType)+" main (int __n, int __m, "+codeGen.dataType+"[] x, "+codeGen.dataType+"[] y) throws Exception {\n");
		int inputParams = main.parameters.size() + main.bitParameters.size();
		String[] inputs = new String[inputParams];
		for(int i=0; i<inputs.length; ++i) {
			if (i < main.bitParameters.size()) {
				String v = main.bitParameters.get(i);
				if(v.equals(getIntParameter(main.parameters.get(0).left))) {
					inputs[i] = "__n";
				} else if(v.equals(getIntParameter(main.parameters.get(1).left))) {
					inputs[i] = "__m";
				} else if(v.equals(getArraySizeParameter(main.parameters.get(0).left))) {
					inputs[i] = "__n / ("+((IntType)((ArrayType)main.parameters.get(0).left).type).bit+")";
				} else if(v.equals(getArraySizeParameter(main.parameters.get(1).left))) {
					inputs[i] = "__m / ("+((IntType)((ArrayType)main.parameters.get(1).left).type).bit+")";
				} else if(v.equals(getArrayIntSizeParameter(main.parameters.get(0).left))) {
					inputs[i] = "__n / ("+((ArrayType)main.parameters.get(0).left).size+")";
				} else if(v.equals(getArrayIntSizeParameter(main.parameters.get(1).left))) {
					inputs[i] = "__m / ("+((ArrayType)main.parameters.get(0).left).size+")";
				} else
					inputs[i] = "0";
				out.println("\t\tint "+v+" = "+inputs[i]+";");
				inputs[i] = v;
			} else {
				int j = i - main.bitParameters.size();
				Type type = main.parameters.get(j).left;
				if(type instanceof IntType) {
					inputs[i] = j == 0 ? "x" : "y";
				} else if(type instanceof ArrayType && ((ArrayType)type).type instanceof IntType) {
					ArrayType at = (ArrayType)type;
					IntType it = (IntType)at.type;
					out.print("\t\tif ( ("+at.size.toString()+") * ("+it.bit+") != "+(j==0 ? "__n" : "__m")+" ) {\n");
					out.print("\t\t\tthrow new RuntimeException(\"input size doesn't match\");\n");
					out.print("\t\t}\n");
					if(at.indexLab.lab == Label.Pub)
						inputs[i] = "com.oblivm.backend.lang.inter.Util.intToArray("+(j==0 ? "x" : "y")+", "+it.bit+", "+at.size+")";
					else
						inputs[i] = "com.oblivm.backend.lang.inter.Util.intToSecArray(env, "+(j==0 ? "x" : "y")+", "+it.bit+", "+at.size+")";
				} else {
					throw new RuntimeException("Unsupported main function.");
				}
			}
		}
		out.print("\t\treturn main(");
		for(int i=0; i<inputs.length; ++i) {
			if(i > 0) out.print(", ");
			out.print(inputs[i]);
		}
		out.println(");");
		out.print("\t}\n");
	}

	@Override
	public void emitMethodsInternal() {
		if(this.inheritWritable(type))
			emitWritableFunctions();

		codeGen.scopeType = this.type;
		for(Method meth : type.getMethods())
			emitAMethod(meth);

		if (this.main != null) {
			emitMain();
		}
	}

}

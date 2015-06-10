/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend.flexsc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oblivm.compiler.ir.ArrayAssign;
import com.oblivm.compiler.ir.ArrayExp;
import com.oblivm.compiler.ir.Assign;
import com.oblivm.compiler.ir.BopExp;
import com.oblivm.compiler.ir.BoxNullExp;
import com.oblivm.compiler.ir.CheckNullExp;
import com.oblivm.compiler.ir.ConstExp;
import com.oblivm.compiler.ir.EnforceBitExp;
import com.oblivm.compiler.ir.FuncCallExp;
import com.oblivm.compiler.ir.GetValueExp;
import com.oblivm.compiler.ir.IRVisitor;
import com.oblivm.compiler.ir.If;
import com.oblivm.compiler.ir.LocalFuncCallExp;
import com.oblivm.compiler.ir.LogExp;
import com.oblivm.compiler.ir.MuxExp;
import com.oblivm.compiler.ir.NativeFuncCallExp;
import com.oblivm.compiler.ir.NewObjExp;
import com.oblivm.compiler.ir.NullExp;
import com.oblivm.compiler.ir.RangeAssign;
import com.oblivm.compiler.ir.RangeExp;
import com.oblivm.compiler.ir.RecExp;
import com.oblivm.compiler.ir.RecordAssign;
import com.oblivm.compiler.ir.Ret;
import com.oblivm.compiler.ir.ReverseRecordAssign;
import com.oblivm.compiler.ir.RopExp;
import com.oblivm.compiler.ir.Seq;
import com.oblivm.compiler.ir.Skip;
import com.oblivm.compiler.ir.SopExp;
import com.oblivm.compiler.ir.UnaryOpExp;
import com.oblivm.compiler.ir.UsingBlock;
import com.oblivm.compiler.ir.VarExp;
import com.oblivm.compiler.ir.Variable;
import com.oblivm.compiler.ir.While;
import com.oblivm.compiler.type.manage.ArrayType;
import com.oblivm.compiler.type.manage.CanonicalName;
import com.oblivm.compiler.type.manage.DummyType;
import com.oblivm.compiler.type.manage.FloatType;
import com.oblivm.compiler.type.manage.FunctionType;
import com.oblivm.compiler.type.manage.IntType;
import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.NativeType;
import com.oblivm.compiler.type.manage.NullType;
import com.oblivm.compiler.type.manage.RecordType;
import com.oblivm.compiler.type.manage.RndType;
import com.oblivm.compiler.type.manage.Type;
import com.oblivm.compiler.type.manage.Unknown;
import com.oblivm.compiler.type.manage.VariableConstant;
import com.oblivm.compiler.type.manage.VariableType;
import com.oblivm.compiler.type.manage.VoidType;
import com.oblivm.compiler.util.Pair;

public class CodeGenVisitor extends IRVisitor<String, Pair<String, String>> {
	public int indent = 0;

	public int tmpId = 0;

	public String dataType;
	public boolean trivial;

	private Type currentType;


	Type scopeType;

	private CanonicalName cn = new CanonicalName();

	public CodeGenVisitor(boolean count, boolean trivial) {
		if(Config.useTemplate) {
			dataType = "t__T";
		} else {
			if (count)
				dataType = "Boolean";
			else
				dataType = "Signal";
		}
		this.trivial = trivial;
	}

	static String indent(int n) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<n; ++i) sb.append("\t");
		return sb.toString();
	}

	@Override
	public Pair<String, String> visit(ArrayExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		ArrayType type = (ArrayType)exp.arr.type;
		if (type.indexLab.lab != Label.Pub) {
			// USING ORAM
			String idx = exp.idx.name;
			if(exp.idx.lab == Label.Pub) {
				sb.append(indent(indent));
				idx = "f_tmp_"+(this.tmpId++);
				sb.append(this.visit(type.type)+" "+idx+" = env.inputOfAlice(Utils.fromInt("+exp.idx.name+", "+type.type.getBits()+"));\n");
			}
			sb.append(indent(indent));
			sb.append(this.visit(type.type)+" "+ret+" = ");
			if(type.type instanceof RecordType)
				sb.append("new "+visit(type.type)+"(");
			else if (type.type instanceof VariableType)
				sb.append("factory"+((VariableType)type.type).name+".newObj(");
			sb.append(exp.arr.name+".read("+idx+")");
			if(type.type instanceof RecordType)
				sb.append(")");
			else if (type.type instanceof VariableType)
				sb.append(")");
			sb.append(";\n");
		} else {
			String ty = visit(type.type); 
			sb.append(indent(indent));
			sb.append(ty+" "+ret+" = "+exp.arr.name+"["+exp.idx.toString()+"];\n");
		}
		return new Pair<String, String>(ret, sb.toString()); 
	}

	@Override
	public Pair<String, String> visit(BopExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		if(exp.getLabels() == Label.Pub) {
			sb.append(indent(indent));
			sb.append("int "+ret+" = "+exp.x1.name);
			if(exp.op == BopExp.Op.Add) {
				sb.append(" + ");
			} else if(exp.op == BopExp.Op.Sub) {
				sb.append(" - ");
			} else if(exp.op == BopExp.Op.Div) {
				sb.append(" / ");
			} else if(exp.op == BopExp.Op.Mod) {
				sb.append(" % ");
			} else if(exp.op == BopExp.Op.Mul) {
				sb.append(" * ");
			} else if(exp.op == BopExp.Op.And) {
				return new Pair<String, String>(exp.x1.name+" & "+exp.x2.name, "");
			} else if(exp.op == BopExp.Op.Xor) {
				return new Pair<String, String>(exp.x1.name+" ^ "+exp.x2.name, "");
			} else if(exp.op == BopExp.Op.Or) {
				return new Pair<String, String>(exp.x1.name+" | "+exp.x2.name, "");
			} else if(exp.op == BopExp.Op.Shl) {
				return new Pair<String, String>(exp.x1.name+" << "+exp.x2.name, "");
			} else if(exp.op == BopExp.Op.Shr) {
				return new Pair<String, String>(exp.x1.name+" >> "+exp.x2.name, "");
			} else
				throw new RuntimeException("Unexpected Operator!");
			sb.append(exp.x2.name+";\n");
		} else {
			if ((exp.op == BopExp.Op.Shl || exp.op == BopExp.Op.Shr) && exp.x2.lab == Label.Pub) {
				if(!(exp.x1.type instanceof IntType))
					throw new RuntimeException("shift operations can be on top of only int values.");
				if(exp.x1.lab != Label.Pub) {
					String x1 = exp.x1.name;
					sb.append(indent(indent));
					if(exp.op == BopExp.Op.Shl)
						sb.append(dataType+"[] "+ret+" = intLib.leftPublicShift("+x1+", "+exp.x2+");\n");
					else if (exp.op == BopExp.Op.Shr)
						sb.append(dataType+"[] "+ret+" = intLib.rightPublicShift("+x1+", "+exp.x2+");\n");
					else
						throw new RuntimeException("Not supported yet.");
				} else {
					sb.append(indent(indent));
					if(exp.op == BopExp.Op.Shl)
						sb.append("int "+ret+" = "+exp.x1.name+" << "+exp.x2+";\n");
					else if(exp.op == BopExp.Op.Shr)
						sb.append("int "+ret+" = "+exp.x1.name+" >> "+exp.x2+";\n");
					else if(exp.op == BopExp.Op.Mul)
						sb.append("int "+ret+" = "+exp.x1.name+" * "+exp.x2+";\n");
					else
						throw new RuntimeException("Not supported yet.");
				}
				return new Pair<String, String>(ret, sb.toString());
			} else {
				String v1 = exp.x1.name;
				if(exp.x1.lab == Label.Pub) {
					sb.append(indent(indent));
					v1 = "f_tmp_"+(this.tmpId++);
					if(exp.x1.type instanceof IntType)
						sb.append(dataType+"[] "+v1+" = env.inputOfAlice(Utils.fromInt("+exp.x1.name+", "+currentType.getBits()+"));\n");
					else
						sb.append(dataType+"[] "+v1+" = floatLib.inputOfAlice("+exp.x1.name+");\n");
				}
				String v2 = exp.x2.name;
				if(exp.x2.lab == Label.Pub) {
					sb.append(indent(indent));
					v2 = "f_tmp_"+(this.tmpId++);
					if(exp.x2.type instanceof IntType)
						sb.append(dataType+"[] "+v2+" = env.inputOfAlice(Utils.fromInt("+exp.x2.name+", "+currentType.getBits()+"));\n");
					else
						sb.append(dataType+"[] "+v2+" = floatLib.inputOfAlice("+exp.x2.name+");\n");
				}

				sb.append(indent(indent));
				String type = dataType;
				if(!exp.x1.type.getBits().isConstant(1) && !exp.x2.type.getBits().isConstant(1))
					type+="[]";
				if(exp.x1.type instanceof IntType)
					sb.append(type+" "+ret+" = intLib.");
				else
					sb.append(type+" "+ret+" = floatLib.");
				if(exp.op == BopExp.Op.Add) {
					sb.append("add");
				} else if(exp.op == BopExp.Op.Sub) {
					sb.append("sub");
				} else if(exp.op == BopExp.Op.Div) {
					sb.append("div");
				} else if(exp.op == BopExp.Op.Mod) {
					sb.append("mod");
				} else if(exp.op == BopExp.Op.Mul) {
					sb.append("multiply");
				} else if(exp.op == BopExp.Op.And) {
					sb.append("and");
				} else if(exp.op == BopExp.Op.Xor) {
					sb.append("xor");
				} else if(exp.op == BopExp.Op.Or) {
					sb.append("or");
				} else
					throw new RuntimeException("Unexpected Operator!");
				sb.append("("+v1+","+v2+");\n");
			}
		}
		return new Pair<String, String>(ret, sb.toString());
	}

	@Override
	public Pair<String, String> visit(UnaryOpExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		if(exp.getLabels() == Label.Pub) {
			sb.append(indent(indent));
			if(exp.op == UnaryOpExp.Op.Neg) {
				sb.append("int "+ret+" = ~"+exp.x.name+";\n");
			} else
				throw new RuntimeException("Unexpected Operator!");
		} else {
			sb.append(indent(indent));
			sb.append(dataType+" "+ret+" = intLib.");
			if(exp.op == UnaryOpExp.Op.Neg) {
				sb.append("not("+exp.x.name+");\n");
			} else
				throw new RuntimeException("Unexpected Operator!");
		}
		return new Pair<String, String>(ret, sb.toString());
	}

	@Override
	public Pair<String, String> visit(ConstExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
//		sb.append(indent(indent));
		if(exp.isInt) {
			if(exp.bits != null && exp.bits.isConstant(1))
				ret = (exp.n != 0 ? "true" : "false");
//				sb.append("boolean "+ret+" = "+(exp.n != 0 ? true : false)+";\n");
			else
				ret = Integer.toString(exp.n);
//				sb.append("int "+ret+" = "+exp.n+";\n");
		} else {
			ret = Double.toString(exp.v);
//			sb.append("double "+ret+" = "+exp.v+";\n");
		}
		return new Pair<String, String>(ret, sb.toString());
	}

	@Override
	public Pair<String, String> visit(MuxExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		Type type = exp.x1.type;
		if(type instanceof IntType) {
			IntType ty = new IntType(type.getBits(), exp.x1.lab.meet(exp.x2.lab));
			if(ty.bit == null)
				ty.bit = exp.x2.getBits();
			type = ty;
		} 
		if(exp.getLabels() != Label.Pub) {
			String v1 = exp.x1.name;
			if(exp.x1.lab == Label.Pub) {
				sb.append(indent(indent));
				v1 = "f_tmp_"+(this.tmpId++);
				if(currentType.getBits().isConstant(1))
					sb.append(dataType+" "+v1+" = env.inputOfAlice("+exp.x1.name+");\n");
				else
					sb.append(dataType+"[] "+v1+" = env.inputOfAlice(Utils.fromInt("+exp.x1.name+", "+currentType.getBits()+"));\n");
			}
			if(!(exp.x1.type.rawType())) {
				v1 = v1+".getBits()";
			}
			String v2 = exp.x2.name;
			if(exp.x2.lab == Label.Pub) {
				sb.append(indent(indent));
				v2 = "f_tmp_"+(this.tmpId++);
				if(currentType.getBits().isConstant(1))
					sb.append(dataType+" "+v2+" = env.inputOfAlice("+exp.x2.name+");\n");
				else
					sb.append(dataType+"[] "+v2+" = env.inputOfAlice(Utils.fromInt("+exp.x2.name+", "+this.currentType.getBits()+"));\n");
			}
			if(!exp.x2.type.rawType()) {
				v2 = v2+".getBits()";
			}
			ret = "";
			if(type instanceof RecordType) {
				ret += "new "+this.constructor((RecordType)type, false)+".newObj(";
				ret += "intLib.mux("+v2+", "+v1+","+exp.b.name+")";
			} else if (type instanceof VariableType) {
				ret += "this.factory"+((VariableType)type).name+".newObj(";
				ret += "intLib.mux("+v2+", "+v1+","+exp.b.name+")";
			} else if (type instanceof NullType || type instanceof DummyType) {
				// TODO deal with null type
				ret = exp.x1.name + ".mux("+exp.b.name+", "+exp.x1.name+")";
				sb = new StringBuffer();
//				DummyType dt = (DummyType)type;
//				String bits = "f_tmp_"+(this.tmpId++);
//				sb.append(indent(indent) + dataType + "[] "+bits+" = intLib.mux("+v2+", "+v1+","+exp.b.name+");\n");
//				String con = this.constructor(dt.type) + ".newObj(Arrays.copyOfRange("
//						+ bits + ", 1, "+bits+".length - 1))"; 
//				if(dt.type instanceof)
//				ret = "new NullableType<"+dataType+", "+targetName(dt.type)+">(env, "+ con + ", "+bits+"[0])";
			} else {
				ret += "intLib.mux("+v2+", "+v1+","+exp.b.name+")";
			}
			if(type instanceof RecordType) {
				ret += ")";
			} else if (type instanceof VariableType) {
				ret += ")";
			}
		} else {
			throw new RuntimeException("should not arriver here.");
		}
		return new Pair<String, String>(ret, sb.toString());
	}

	@Override
	public Pair<String, String> visit(RopExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		if(exp.getLabels() != Label.Pub) {
			VariableConstant bits = exp.x1.getBits();
			if(exp.x2.getBits() != null && !(exp.x2.getBits() instanceof Unknown))
				bits = exp.x2.getBits();
			IntType t1 = (IntType)exp.x1.type;
			IntType t2 = (IntType)exp.x2.type;
			t1.bit = bits;
			t2.bit = bits;

			String type = dataType;
			if(!bits.isConstant(1)) type += "[]";

			String v1 = exp.x1.name;
			if(exp.x1.lab == Label.Pub) {
				sb.append(indent(indent));
				v1 = "f_tmp_"+(this.tmpId++);
				if(bits.isConstant(1))
					sb.append(type+" "+v1+" = env.inputOfAlice("+exp.x1.name+");\n");
				else 
					sb.append(type+" "+v1+" = env.inputOfAlice(Utils.fromInt("+exp.x1.name+", "+bits+"));\n");
			}
			String v2 = exp.x2.name;
			if(exp.x2.lab == Label.Pub) {
				sb.append(indent(indent));
				v2 = "f_tmp_"+(this.tmpId++);
				if(bits.isConstant(1))
					sb.append(type+" "+v2+" = env.inputOfAlice("+exp.x2.name+");\n");
				else 
					sb.append(type+" "+v2+" = env.inputOfAlice(Utils.fromInt("+exp.x2.name+", "+bits+"));\n");
			}
			sb.append(indent(indent));
			if(exp.op == RopExp.Op.Eq)
				sb.append(dataType+" "+ret+" = intLib.eq("+v1+", "+v2+");\n");
			else if(exp.op == RopExp.Op.Ne)
				sb.append(dataType+" "+ret+" = intLib.not(intLib.eq("+v1+", "+v2+"));\n");
			else if(exp.op == RopExp.Op.Ge)
				sb.append(dataType+" "+ret+" = intLib.geq("+v1+", "+v2+");\n");
			else if(exp.op == RopExp.Op.Gt)
				sb.append(dataType+" "+ret+" = intLib.not(intLib.leq("+v1+", "+v2+"));\n");
			else if(exp.op == RopExp.Op.Le)
				sb.append(dataType+" "+ret+" = intLib.leq("+v1+", "+v2+");\n");
			else if(exp.op == RopExp.Op.Lt)
				sb.append(dataType+" "+ret+" = intLib.not(intLib.geq("+v1+", "+v2+"));\n");
			else
				throw new RuntimeException("Not supported yet.");
		} else {
			sb.append(indent(indent));
			if(exp.op == RopExp.Op.Eq)
				sb.append("boolean "+ret+" = "+exp.x1.name+" == "+exp.x2.name+";\n");
			else if(exp.op == RopExp.Op.Ne)
				sb.append("boolean "+ret+" = "+exp.x1.name+" != "+exp.x2.name+";\n");
			else if(exp.op == RopExp.Op.Ge)
				sb.append("boolean "+ret+" = "+exp.x1.name+" >= "+exp.x2.name+";\n");
			else if(exp.op == RopExp.Op.Gt)
				sb.append("boolean "+ret+" = "+exp.x1.name+" > "+exp.x2.name+";\n");
			else if(exp.op == RopExp.Op.Le)
				sb.append("boolean "+ret+" = "+exp.x1.name+" <= "+exp.x2.name+";\n");
			else if(exp.op == RopExp.Op.Lt)
				sb.append("boolean "+ret+" = "+exp.x1.name+" < "+exp.x2.name+";\n");
			else
				throw new RuntimeException("Not supported yet.");
		}
		return new Pair<String, String>(ret, sb.toString());
	}

	@Override
	public Pair<String, String> visit(SopExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		if(exp.getLabels() != Label.Pub) {
			String x1 = exp.x1.name;
			sb.append(indent(indent));
			if(exp.op == SopExp.Op.Shl)
				sb.append(dataType+"[] "+ret+" = intLib.leftPublicShift("+x1+", "+exp.bit+");\n");
			else if (exp.op == SopExp.Op.Shr)
				sb.append(dataType+"[] "+ret+" = intLib.rightPublicShift("+x1+", "+exp.bit+");\n");
			else
				throw new RuntimeException("Not supported yet.");
		} else {
			sb.append(indent(indent));
			if(exp.op == SopExp.Op.Shl)
				sb.append("int "+ret+" = "+exp.x1.name+" << "+exp.bit+";\n");
			else if(exp.op == SopExp.Op.Shr)
				sb.append("int "+ret+" = "+exp.x1.name+" >> "+exp.bit+";\n");
			else if(exp.op == SopExp.Op.Mul)
				sb.append("int "+ret+" = "+exp.x1.name+" * "+exp.bit+";\n");
			else
				throw new RuntimeException("Not supported yet.");
		}
		return new Pair<String, String>(ret, sb.toString());
	}

	@Override
	public Pair<String, String> visit(VarExp exp) {
		return new Pair<String, String>(exp.var.name, "");
	}

	@Override
	public String visit(ArrayAssign arrayAssign) {
		if(arrayAssign.value.type instanceof NullType) {
			if(arrayAssign.indexLab == Label.Pub) {
				return indent(indent) + arrayAssign.name.name + "["+arrayAssign.idx.name+"].isNull = env.inputOfAlice(false);\n";
			} else {
				String v1 = arrayAssign.idx.name;
				this.currentType = new IntType(32, Label.Secure);
				if(arrayAssign.idx.lab == Label.Pub) {
					v1 = "env.inputOfAlice(Utils.fromInt("+arrayAssign.idx.name+", "+this.currentType.getBits()+"))";
				}
				ArrayType at = (ArrayType)arrayAssign.name.type;
				String v2 = this.constructor(at.type);
				return indent(indent) + arrayAssign.name.name + ".write("+v1+", "+v2+");\n";
			}
		}

		StringBuffer sb = new StringBuffer();
		ArrayType type = (ArrayType)arrayAssign.name.type;
		Type old = this.currentType;
		if(type.indexLab.lab != Label.Pub) {
			// ORAM
			String v1 = arrayAssign.idx.name;
			this.currentType = new IntType(32, Label.Secure);
			if(arrayAssign.idx.lab == Label.Pub) {
				sb.append(indent(indent));
				v1 = "f_tmp_"+(this.tmpId++);
				sb.append(dataType+"[] "+v1+" = env.inputOfAlice(Utils.fromInt("+arrayAssign.idx.name+", "+this.currentType.getBits()+"));\n");
			}
			String v2 = arrayAssign.value.name;
			if(arrayAssign.value.lab == Label.Pub) {
				if(arrayAssign.value.type instanceof IntType) {
					this.currentType = new IntType(type.type.getBits(), Label.Secure);
					sb.append(indent(indent));
					v2 = "f_tmp_"+(this.tmpId++);
					sb.append(dataType+"[] "+v2+" = env.inputOfAlice(Utils.fromInt("+arrayAssign.value.name+", "+this.currentType.getBits()+"));\n");
				} else if(arrayAssign.value.type instanceof FloatType) {
					this.currentType = new FloatType(type.type.getBits(), Label.Secure);
					sb.append(indent(indent));
					v2 = "f_tmp_"+(this.tmpId++);
					sb.append(dataType+"[] "+v2+" = floatLib.inputOfAlice("+arrayAssign.value.name+");\n");
				} else if(arrayAssign.value.type.getBits() != null){
					sb.append(indent(indent));
					v2 = "f_tmp_"+(this.tmpId++);
					sb.append(dataType+"[] "+v2+" = "+arrayAssign.value.name+".toBits();\n");
				} else {
					arrayAssign.value.type.getBits();
					throw new RuntimeException(type+" is not supported yet.");
				}
			}
			if(!(arrayAssign.value.type instanceof IntType))
				v2 += ".getBits()";
			String cmd = arrayAssign.name+".write("+v1+","+v2+");\n";
			sb.append(indent(this.indent));
			sb.append(cmd);
		} else {
			String v = arrayAssign.value.name;
			if(type.getLabel() != Label.Pub && arrayAssign.value.lab == Label.Pub) {
				sb.append(indent(indent));
				v = "f_tmp_"+(this.tmpId++);
				if(type.type instanceof IntType) {
					if(type.type.getBits().isConstant(1))
						sb.append(visit(type.type)+" "+v+" = env.inputOfAlice("+arrayAssign.value.name+");\n");
					else
						sb.append(visit(type.type)+" "+v+" = env.inputOfAlice(Utils.fromInt("+arrayAssign.value.name+", "+type.type.getBits()+"));\n");
				} else if(type.type instanceof FloatType) {
					sb.append(visit(type.type)+" "+v+" = floatLib.inputOfAlice("+arrayAssign.value.name+");\n");
				} else
					throw new RuntimeException("Unknown Type Exception.");
			}
			sb.append(indent(this.indent));
			sb.append(arrayAssign.name.name+"["+arrayAssign.idx.name+"]="+v+";\n");
		}
		this.currentType = old;
		return sb.toString();
	}

	@Override
	public String visit(Assign assign) {
		if(assign.name.type instanceof NullType)
			return "";
		if(assign.exp instanceof BoxNullExp) {
			BoxNullExp be = (BoxNullExp)assign.exp;
			if(be.var.type instanceof NullType) {
				return indent(indent) + assign.name.name + ".isNull = env.inputOfAlice(false);\n";
			}
		} else if(assign.exp instanceof NullExp ||
				(assign.exp instanceof VarExp) && 
				((VarExp)assign.exp).var.type instanceof NullType)
				{
			return indent(indent) + assign.name.name + ".isNull = env.inputOfAlice(false);\n";
		}

		Type old = this.currentType;

		this.currentType = assign.name.type;

		String type = visit(assign.name.type);

		if(assign.name.lab == Label.Secure && type.equals("int"))
		{
			System.out.println("Wrong here.");
		}
		StringBuffer sb = new StringBuffer();
		if(assign.name.toHaveType && assign.withTypeDef) {
			if(this.currentType != VoidType.get()) {
				if(assign.lab != Label.Pub && assign.exp.getLabels() == Label.Pub) {
					Pair<String, String> tmp = visit(assign.exp);
					sb.append(tmp.right);
					sb.append(indent(indent));
					if(assign.name.type instanceof IntType) {
						if(assign.name.type.getBits().isConstant(1))
							sb.append(type + " "+assign.name.name+" = env.inputOfAlice("+tmp.left+")");
						else
							sb.append(type + " "+assign.name.name+" = env.inputOfAlice(Utils.fromInt("+tmp.left+", "+assign.name.type.getBits()+"))");
					} else if(assign.name.type instanceof FloatType) {
						sb.append(type + " "+assign.name.name+" = floatLib.inputOfAlice("+tmp.left+")");
					} else
						throw new RuntimeException("Not supported yet.");
					//					if(!type.trim().endsWith("[]"))
					//						sb.append("[0]");
					sb.append(";\n");
				} else {
					if (assign.name.type instanceof DummyType && assign.exp instanceof MuxExp) {
						DummyType dt = (DummyType) assign.name.type;
						MuxExp me = (MuxExp) assign.exp;
						if(me.x1.type instanceof NullType && me.x2.type instanceof NullType) {
							return indent(indent) + type + " " + assign.name.name + " = " + this.constructor(dt) + ";\n";
						} else if(me.x1.type instanceof NullType && !(me.x2.type instanceof NullType)) {
							return indent(indent) + type + " " + assign.name.name + " = " 
									+ "new NullableType<"+dataType+", "+targetName(dt.type)+">(env, "+me.x2.name+
									".value, intLib.mux(env.inputOfAlice(false), "
									+ me.x2.name+".isNull, " + me.b.name+ "));\n";
						} else if(!(me.x1.type instanceof NullType) && (me.x2.type instanceof NullType)) {
							return indent(indent) + type + " " + assign.name.name + " = " 
									+ "new NullableType<"+dataType+", "+targetName(dt.type)+">(env, "+me.x1.name
									+ ".value, intLib.mux("
									+ me.x1.name+".isNull, env.inputOfAlice(false), " + me.b.name+ "));\n";
						}
					}
					Pair<String, String> tmp = visit(assign.exp);
					String value = tmp.left;
					sb.append(tmp.right);
					sb.append(indent(indent));
					if(assign.exp.getLabels() == Label.Secure && assign.name.lab == Label.Pub) {
						value = "env.outputToAlice("+value+")";
						if(assign.name.getBits() == null || !assign.name.getBits().isConstant(1))
							value = "Utils.toInt("+value+")";
					}
					sb.append(type+" "+assign.name.name+" = "+value+";\n");
				}
			}
		} else {
			if(this.currentType != VoidType.get()) {
				if(assign.lab != Label.Pub && assign.exp.getLabels() == Label.Pub) {
					Pair<String, String> tmp = visit(assign.exp);
					sb.append(tmp.right);
					sb.append(indent(indent));
					if(assign.name.type instanceof IntType) {
						if(assign.name.type.getBits().isConstant(1))
							sb.append(assign.name.name+" = env.inputOfAlice("+tmp.left+");\n");
						else
							sb.append(assign.name.name+" = env.inputOfAlice(Utils.fromInt("+tmp.left+", "+assign.name.type.getBits()+"));\n");
					} else {
						sb.append(assign.name.name+" = floatLib.inputOfAlice("+tmp.left+");\n");
					}
				} else {
					if (assign.name.type instanceof DummyType && assign.exp instanceof MuxExp) {
						DummyType dt = (DummyType) assign.name.type;
						MuxExp me = (MuxExp) assign.exp;
						if(me.x1.type instanceof NullType && me.x2.type instanceof NullType) {
							return indent(indent) + assign.name.name + ".isNull = env.inputOfAlice(false);\n";
						} else if(me.x1.type instanceof NullType && !(me.x2.type instanceof NullType)) {
							return indent(indent) + assign.name.name + ".value = " + me.x2.name + ".value;\n" +
									indent(indent) + assign.name.name + ".isNull = intLib.mux(env.inputOfAlice(false), "
									+ me.x2.name+".isNull, " + me.b.name+ ");\n";
						} else if(!(me.x1.type instanceof NullType) && (me.x2.type instanceof NullType)) {
							return indent(indent) + assign.name.name + ".value = " + me.x1.name + ".value;\n" +
									indent(indent) + assign.name.name + ".isNull = intLib.mux("
									+ me.x1.name+".isNull, env.inputOfAlice(false), " + me.b.name+ ");\n";
						}
					}
					Pair<String, String> tmp = visit(assign.exp);
					String value = tmp.left;
					if(assign.exp.getLabels() == Label.Secure && assign.name.lab == Label.Pub) {
						value = "env.outputToAlice("+value+")";
						if(assign.name.getBits() == null || !assign.name.getBits().isConstant(1))
							value = "Utils.toInt("+value+")";
					}
					sb.append(tmp.right);
					sb.append(indent(indent));
					sb.append(assign.name.name+" = "+value+";\n");
				}
			}
		}

		this.currentType = old;
		return sb.toString();
	}

	@Override
	public String visit(If ifStmt) {
		StringBuffer sb = new StringBuffer();
		sb.append(indent(indent));
		sb.append("if("+ifStmt.cond.name+") {\n");
		indent++;
		sb.append(this.visit(ifStmt.trueBranch));
		indent--;
		sb.append(indent(indent));
		sb.append("} else {\n");
		indent++;
		sb.append(this.visit(ifStmt.falseBranch));
		indent--;
		sb.append(indent(indent));
		sb.append("}\n");
		return sb.toString(); 	
	}

	@Override
	public String visit(Seq seq) {
		String s1 = visit(seq.s1);
		String s2 = visit(seq.s2);
		return  s1 + s2;
	}

	@Override
	public String visit(Skip skip) {
		return "";
	}

	@Override
	public String visit(While whileStmt) {
		StringBuffer sb = new StringBuffer();
		sb.append(indent(indent));
		sb.append("while("+whileStmt.cond.name+") {\n");
		indent++;
		sb.append(this.visit(whileStmt.body));
		indent--;
		sb.append(indent(indent));
		sb.append("}\n");
		return sb.toString();	
	}

	@Override
	public Pair<String, String> visit(RecExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		RecordType type = (RecordType)exp.base.type;
		String ty = visit(type.fields.get(exp.field));
		sb.append(indent(indent));
		sb.append(ty+" "+ret+" = "+exp.base.name+"."+exp.field+";\n");
		return new Pair<String, String>(ret, sb.toString()); 

	}

	@Override
	public Pair<String, String> visit(FuncCallExp exp) {
		StringBuffer precomputing = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		String ty = visit(exp.type);
		sb.append(indent(indent));
		String base = "noclass";
		if(exp.base != null)
			base = exp.base.name;
		if(exp.type != VoidType.get())
			sb.append(ty+" "+ret+" = ");
		if(exp.isNative) {
			if(exp.type instanceof VariableType) {
				sb.append("factory"+((VariableType)exp.type).name+".newObj(");
			} else if (exp.type instanceof RecordType) {
				sb.append(this.constructor(exp.type)+".newObj(");
			}
		}
		sb.append(base+"."+exp.func+"(");
		boolean f = true;
		for(int i=0; i<exp.bitParameters.size(); ++i) {
			if(f) f = false;
			else sb.append(", ");
			sb.append(exp.bitParameters.get(i).toString());
		}
		for(int i=0; i<exp.inputs.size(); ++i) {
			if(f) f = false;
			else sb.append(", ");
			Variable var = exp.inputs.get(i);
			if(exp.isNative) {
				if(var.type instanceof FunctionType) {
					sb.append(var.name);
				} else if(var.type instanceof ArrayType) {
					// Handle convert array to Boolean[][]
					ArrayType at = (ArrayType)var.type;
					String tmp = "f_tmp_"+(this.tmpId++);
					precomputing.append(indent(indent)+dataType+"[][] "+tmp+" = new "+dataType+"["+at.size+"][];\n");
					precomputing.append(indent(indent)+"for(int _tmp_i=0; _tmp_i<"+at.size+"; ++_tmp_i) {\n");
					precomputing.append(indent(indent+1)+tmp+"[_tmp_i] = "+var.name+"[_tmp_i].getBits();\n");
					precomputing.append(indent(indent)+"}\n");
					sb.append(tmp);
				} else if(var.type instanceof NativeType) {
					sb.append(exp.inputs.get(i));
				} else if(!(var.type instanceof IntType) && 
						!(var.type instanceof RndType) &&
						!(var.type instanceof FloatType) &&
						exp.isNative) {
					sb.append(exp.inputs.get(i)+".getBits()");
				} else {
					sb.append(exp.inputs.get(i));
				}
			} else {
				if(var.type instanceof FunctionType && ((FunctionType)var.type).global) {
					sb.append(this.constructor(var.type));
				} else {
					if(var.type.getLabel() == Label.Pub && exp.fty.inputTypes.get(i).getLabel() == Label.Secure) {
						if(var.type.getBits().isConstant(1))
							sb.append("env.inputOfAlice(" + var.name+")");
						else 
							sb.append("env.inputOfAlice(Utils.fromInt(" + var.name+", "+var.type.getBits()+"))");
					} else
						sb.append(var.name);
				}
			}
		}
		sb.append(")");
		if(exp.isNative) {
			if(exp.type instanceof VariableType) {
				sb.append(")");
			} else if (exp.type instanceof RecordType) {
				sb.append(")");
			}
		}
		sb.append(";\n");
		return new Pair<String, String>(ret, precomputing.toString()+sb.toString()); 
	}

	@Override
	public Pair<String, String> visit(NativeFuncCallExp exp) {
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		String ty = visit(exp.type);
		sb.append(indent(indent));
		String base = "NoClass";
		if(exp.base != null)
			base = exp.base.name;
		sb.append(ty+" "+ret+" = ");
		if(exp.type instanceof RecordType)
			sb.append("new "+this.constructor((RecordType)exp.type, false)+".newObj(");
		sb.append(base+"."+exp.nativeName+"(");
		for(int i=0; i<exp.inputs.size(); ++i) {
			if(i > 0) sb.append(", ");
			sb.append(exp.inputs.get(i));
		}
		sb.append(")");
		if(exp.type instanceof RecordType)
			sb.append(")");
		sb.append(";\n");
		return new Pair<String, String>(ret, sb.toString()); 
	}

	@Override
	public String visit(RecordAssign assign) {
		StringBuffer sb = new StringBuffer();
		String v = assign.value.name;
		RecordType type = (RecordType)(assign.base.type);
		if(type.fields.get(assign.field).getLabel() != Label.Pub && assign.value.lab == Label.Pub && assign.value.type instanceof IntType) {
			sb.append(indent(indent));
			v = "f_tmp_"+(this.tmpId++);
			if(type.fields.get(assign.field).getBits().isConstant(1))
				sb.append(dataType+" "+v+" = env.inputOfAlice("+assign.value.name+");\n");
			else 
				sb.append(dataType+"[] "+v+" = env.inputOfAlice(Utils.fromInt("+assign.value.name+", "+type.fields.get(assign.field).getBits()+"));\n");
		}
		sb.append(indent(indent)+assign.base.name+"."+assign.field+" = "+v+";\n");
		return sb.toString();
	}

	@Override
	public String visit(ReverseRecordAssign assign) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<assign.var.length; ++i) {
			sb.append(indent(indent));
			sb.append(visit(assign.var[i].type));
			sb.append(" "+assign.var[i].name+" = "+assign.base.name+"."+assign.fields[i]+";\n");
		}
		return sb.toString();
	}

	@Override
	public String visit(Ret ret) {
		return indent(indent)+"return " + ret.exp.name+";\n";
	}

	public String visit(Type type) {
		if(type instanceof ArrayType)
			return visit((ArrayType)type);
		else if(type instanceof IntType)
			return visit((IntType)type);
		else if(type instanceof NativeType)
			return visit((NativeType)type);
		else if(type instanceof RecordType)
			return visit((RecordType)type);
		else if(type instanceof VoidType)
			return visit((VoidType)type);
		else if(type instanceof VariableType) {
			return visit((VariableType)type);
		} else if(type instanceof FloatType) {
			return visit((FloatType)type);
		} else if(type instanceof RndType) {
			return visit((RndType)type);
		} else if(type instanceof FunctionType) {
			return visit((FunctionType)type);
		} else if(type instanceof NullType) {
			return visit((NullType)type);
		} else if(type instanceof DummyType) {
			return visit((DummyType)type);
		} else
			throw new RuntimeException("Unknown type!");
	}

	public String visit(ArrayType type) {
		if(type.indexLab.lab != Label.Pub) {
			return "SecureArray<"+dataType+">";
		}
		return visit(type.type)+"[]";
	}

	public String visit(IntType type) {
		if(type.getLabel() != Label.Pub) {
			if(type.getBits().isConstant(1))
				return dataType;
			else
				return dataType+"[]";
		} else {
			if(type.getBits() != null && type.getBits().isConstant(1))
				return "boolean";
			else
				return "int";
		}
	}

	public String visit(FloatType type) {
		if(type.getLabel() != Label.Pub) {
			if(type.getBits().isConstant(1))
				return dataType;
			else
				return dataType+"[]";
		} else {
			return "double";
		}
	}

	public String visit(RndType type) {
		if(type.getLabel() != Label.Pub) {
			if(type.getBits().isConstant(1))
				return dataType;
			else
				return dataType+"[]";
		} else {
			if(type.getBits().isConstant(1))
				return "boolean";
			else
				return "int";
		}
	}

	public String visit(FunctionType type) {
		String ret = cn.visit(type);
		if(type.typeParameters.size() > 0) {
			ret += "<";
			boolean f = true;
			if(Config.useTemplate) {
				ret += this.dataType;
				f = false;
			}
			for(int i=0; i<type.typeParameters.size(); ++i) {
				if(f) f = false; else ret += ", ";
				ret += type.typeParameters.get(i);
			}
			ret += ">";
		}
		return ret;
	}

	public String visit(NativeType type) {
		return type.nativeName+"<"+dataType+">";
	}

	public String visit(RecordType type) {
		String name = type.name;
		if(Config.useTemplate) {
			if(type.typeParameter != null && type.typeParameter.size() != 0) {
				name+="<"+this.dataType;
				boolean f = true;
				for(int i=0; i<type.typeParameter.size(); ++i){
					name+=", ";
					name+=visit(type.typeParameter.get(i));
				}
				name+=">";
			} else
				name += "<"+this.dataType+">";
		} else {
			if(type.typeParameter != null && type.typeParameter.size() != 0) {
				name+="<";
				boolean f = true;
				for(int i=0; i<type.typeParameter.size(); ++i){
					if(f) f = false;
					else name+=", ";
					name+=visit(type.typeParameter.get(i));
				}
				name+=">";
			}
		}
		return name;
	}

	public String visit(VoidType type) {
		return "void";
	}

	public String visit(VariableType type) {
		return isDefine ? type.name + " extends IWritable<"+type.name+", "+dataType+">" : type.name;
	}

	public String visit(NullType type) {
		throw new RuntimeException("Shouldn't visit here.");
	}

	public String visit(DummyType type) {
		return "NullableType<"+dataType+", "+targetName(type.type)+">";
	}

	@Override
	public Pair<String, String> visit(NewObjExp exp) {
		String ret = "f_tmp_"+(this.tmpId++);
		StringBuffer sb = new StringBuffer();
		sb.append(indent(indent));
		String type = visit(exp.type);
		sb.append(type+" "+ret+" = new "+this.constructor(exp.type, false)+";\n");
		for(Map.Entry<String, Variable> var : exp.initialValue.entrySet()) {
			sb.append(indent(indent));
			if(var.getValue().lab == Label.Pub && exp.type.fields.get(var.getKey()).getLabel() == Label.Secure) {
				if(var.getValue().type instanceof IntType) {
					if(var.getValue().getBits().isConstant(1))
						sb.append(ret+"."+var.getKey()+" = env.inputOfAlice("+var.getValue()+");\n");
					else
						sb.append(ret+"."+var.getKey()+" = env.inputOfAlice(Utils.fromInt("+var.getValue()+", "+var.getValue().type.getBits()+"));\n");
				} else {
					sb.append(ret+"."+var.getKey()+" = floatLib.inputOfAlice("+var.getValue()+");\n");
				}
			} else
				sb.append(ret+"."+var.getKey()+" = "+var.getValue()+";\n");
		}
		return new Pair<String, String>(ret, sb.toString());
	}

	public boolean isDefine = false;

	public String constructor(Type type) {
		if(type instanceof DummyType) {
			DummyType dt = (DummyType)type;
			String con = constructor(dt.type);
			if(dt.type instanceof IntType)
				con = "new BoxedInt<@dataType>(env, "+con+")";
			if(dt.type instanceof RndType)
				con = "new BoxedRnd<@dataType>(env, "+con+")";
			if(dt.type instanceof FloatType)
				con = "new BoxedFloat<@dataType>(env, "+con+")";
			con = con.replaceAll("@dataType", dataType);
			return "new NullableType<"+dataType+", "+targetName(dt.type)+">(env, "+con+", env.inputOfAlice(false))";
		} else if(type instanceof IntType) {
			IntType it = (IntType)type;
			String s;
			if(it.getBits().isConstant(1))
				s = "false";
			else
				s = "0";

			if(it.getLabel() != Label.Pub) {
				if(!it.getBits().isConstant(1))
					s = "env.inputOfAlice(Utils.fromInt(0, "+it.getBits()+"))";
				else
					s = "env.inputOfAlice(false)";

			}

			return s;
		} else if(type instanceof RndType) {
			RndType it = (RndType)type;
			String s;
			if(it.getBits().isConstant(1))
				s = "false";
			else
				s = "0";

			if(it.getLabel() != Label.Pub) {
				s = "intLib.randBools("+it.getBits()+")";
			}

			return s;
		} else if(type instanceof FloatType) {
			FloatType it = (FloatType)type;
			String s = "0.0";

			if(it.getLabel() != Label.Pub) {
				s = "floatLib.inputOfAlice(0.0)";
			}

			return s;
		} else if (type instanceof RecordType) {
			return "new "+this.constructor((RecordType)type, false);
		} else if (type instanceof VariableType) {
			String name = ((VariableType)type).name;
			return "factory"+name+".newObj(null)";
		} else if (type instanceof ArrayType) {
			ArrayType at = (ArrayType)type;
			if(at.indexLab.lab == Label.Pub) {
				String cons = constructor(at.type);
				StringBuffer sb = new StringBuffer();
				if(at.type instanceof VariableType) {
					String typ = visit(at.type);
					sb.append("("+typ+"[])Array.newInstance((Class<"+typ+">)factory"+typ+".getClass(), "+at.size+")");
				} else if (at.type instanceof RecordType) {
					String typ = visit(at.type);
					String con = this.constructor(at.type);
					sb.append("("+typ+"[])Array.newInstance((Class<"+typ+">)("+con+".getClass()), "+at.size+")");
				} else {
					List<VariableConstant> number = new ArrayList<VariableConstant>();
					Type nt = type;
					while(nt instanceof ArrayType) {
						at = (ArrayType)nt;
						number.add(at.size);
						nt = at.type;
					}

					if((nt instanceof IntType) ||
							(nt instanceof RndType) ||
							(nt instanceof FloatType)
							) {
						sb.append("env.newTArray(");
						for(int i=0; i<number.size(); ++i) {
							if(i>0) sb.append(",");
							sb.append(number.get(i));
						}
						sb.append(", 0)");
					} else {
						sb.append("new "+nativeName(nt)+"");
						for(int i=0; i<number.size(); ++i) {
							sb.append("["+number.get(i)+"]");
						}
						//						throw new RuntimeException("Not supported nested array of UDT yet.");
					}
					//					String ty = visit(at.type);
					//					if(ty.endsWith("[]")) {
					//						String pre = ty, ed = "";
					//						while(pre.endsWith("[]")) {
					//							pre = pre.substring(0, pre.length() - 2);
					//							ed = "[]"+ed;
					//						}
					//						sb.append("new "+pre + "["+at.size+"]"+ed);
					//					}
					//					else {
					//						//						sb.append("new "+ty+"["+at.size+"]");
					//						sb.append("env.newTArray("+at.size+")");
					//					}
				}
				// TODO
				//				for(int i=0; i<at.size; ++i) {
				//					if(i > 0) sb.append(", ");
				//					sb.append(cons);
				//				}
				//				sb.append("}");
				return sb.toString();
			} else {
				String s = "SecureArray<"+dataType+">"; 
				return "new "+s+"(env, "+at.size+", "+at.type.getBits()+")";
			}
		} else if (type instanceof FunctionType) {
			FunctionType fty = (FunctionType)type;
			StringBuffer sb = new StringBuffer();
			sb.append("((");
			boolean f = false;
			for(int i=0; i<fty.bitParameters.size(); ++i) {
				if(f) sb.append(", "); else f = true;
				sb.append("_bIt_"+i);
			}
			for(int i=0; i<fty.inputTypes.size(); ++i) {
				if(f) sb.append(", "); else f = true;
				sb.append("_iNpUt_"+i);
			}
			sb.append(") -> ");
			if(!this.scopeType.name.equals("NoClass")) {
				sb.append("noclass.");
			}
			sb.append(fty.name+"(");
			f = false;
			for(int i=0; i<fty.bitParameters.size(); ++i) {
				if(f) sb.append(", "); else f = true;
				sb.append("_bIt_"+i);
			}
			for(int i=0; i<fty.inputTypes.size(); ++i) {
				if(f) sb.append(", "); else f = true;
				sb.append("_iNpUt_"+i);
			}
			sb.append("))");
			return sb.toString();
		} else {
			return null;
		}
	}

	public String constructor(RecordType type, boolean withDef) {
		StringBuffer sb = new StringBuffer();
		if(withDef) {
			sb.append(type.name);
		} else {
			sb.append(visit(type));
		}
		sb.append("(");
		for(int i=0; i<Config.types.length; ++i) {
			if(i > 0)
				sb.append(", ");
			if(withDef) sb.append(Config.types[i].replace("@dataType", dataType)+" ");
			sb.append(Config.fields[i]);
			break;
		}
		for(int i=0; i<type.bits.size(); ++i) {
			sb.append(", ");
			if(withDef) sb.append("int ");
			sb.append(type.bits.get(i).toString());
		}
		if(type.typeParameter != null) {
			for(Type tt : type.typeParameter) {
				if(tt instanceof VariableType) {
					VariableType vt = (VariableType)tt;
					sb.append(", ");
					if(withDef) sb.append(vt.name+" ");
					sb.append("factory"+vt.name);
				} else {
					RecordType rt = (RecordType)tt;
					if(withDef) 
						throw new RuntimeException("Should emit type definition in a record constructor.");
					sb.append(", new ");
					sb.append(this.constructor(rt, false));
				}
			}
		}
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			if(ent.getValue().getBits() == null) {
				sb.append(", ");
				if(withDef)
					sb.append(visit(ent.getValue())+" ");
				sb.append(this.constructor(ent.getValue()));

			}
		}
		sb.append(")");
		return sb.toString();
	}

	public String constructorWithNonStruct(RecordType type) {
		StringBuffer sb = new StringBuffer();
		sb.append(type.name);
		sb.append("(");
		for(int i=0; i<Config.types.length; ++i) {
			if(i > 0)
				sb.append(", ");
			sb.append(Config.types[i].replace("@dataType", dataType)+" ");
			sb.append(Config.fields[i]);
			break;
		}
		// bit variables
		for(int i=0; i<type.bits.size(); ++i) {
			sb.append(", ");
			sb.append("int "+type.bits.get(i).toString());
		}
		// factories
		if(type.typeParameter != null) {
			for(Type tt : type.typeParameter) {
				VariableType vt = (VariableType)tt;
				sb.append(", ");
				sb.append(vt.name+" ");
				sb.append("factory"+vt.name);
			}
		}
		for(Map.Entry<String, Type> ent : type.fields.entrySet()) {
			if(ent.getValue().getBits() == null) {
				sb.append(", ");
				sb.append(visit(ent.getValue())+" ");
				sb.append(ent.getKey());

			}
		}
		sb.append(")");
		return sb.toString();
	}


	public String nativeName(Type type) {
		if(type instanceof ArrayType)
			return nativeName((ArrayType)type);
		else if(type instanceof IntType)
			return nativeName((IntType)type);
		else if(type instanceof NativeType)
			return nativeName((NativeType)type);
		else if(type instanceof RecordType)
			return nativeName((RecordType)type);
		else if(type instanceof VoidType)
			return nativeName((VoidType)type);
		else if(type instanceof VariableType) {
			return nativeName((VariableType)type);
		} else if(type instanceof DummyType) {
			return "NullableType";
		} else
			throw new RuntimeException("Unknown type!");
	}

	public String nativeName(ArrayType type) {
		if(type.indexLab.lab != Label.Pub) { 
			return "SecureArray<"+dataType+">";
		}
		return visit(type.type)+"[]";
	}

	public String nativeName(IntType type) {
		if(type.getLabel() != Label.Pub) {
			if(type.getBits().isConstant(1))
				return dataType;
			else
				return dataType+"[]";
		} else {
			if(type.getBits().isConstant(1))
				return "boolean";
			else
				return "int";
		}
	}

	public String nativeName(NativeType type) {
		return type.nativeName+"<"+dataType+">";
	}

	public String nativeName(RecordType type) {
		return dataType+"[]";
	}

	public String nativeName(VoidType type) {
		return "void";
	}

	public String nativeName(VariableType type) {
		return dataType+"[]";
	}

	@Override
	public Pair<String, String> visit(LocalFuncCallExp exp) {
		StringBuffer precomputing = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		String ret = "f_tmp_"+(this.tmpId++);
		String ty = visit(exp.type);
		sb.append(indent(indent));
		if(exp.type != VoidType.get())
			sb.append(ty+" "+ret+" = ");
		sb.append(exp.func+".calc(");
		for(int i=0; i<exp.inputs.size(); ++i) {
			if(i > 0) sb.append(", ");
			Variable var = exp.inputs.get(i);
			sb.append(var.name);
		}
		sb.append(")");
		sb.append(";\n");
		return new Pair<String, String>(ret, precomputing.toString()+sb.toString()); 
	}

	@Override
	public Pair<String, String> visit(LogExp exp) {
		if(exp.lab == Label.Pub) {
			return new Pair<String, String>("Utils.logFloor("+exp.exp+")", "");
		} else {
			return new Pair<String, String>(exp.exp+".length", "");
		}
	}

	@Override
	public Pair<String, String> visit(RangeExp exp) {
		if(exp.lab == Label.Pub) {
			if(exp.rr == null)
				return new Pair<String, String>("(("+exp.base+">>("+exp.rl+")) & 1)", "");
			else
				return new Pair<String, String>("(("+exp.base+" & ((1<<("+exp.rr+")) - 1))>>("+exp.rl+"))", "");
		} else {
			if(exp.rr == null)
				return new Pair<String, String>(exp.base+"["+exp.rl+"]", "");
			else
				return new Pair<String, String>("Arrays.copyOfRange("+exp.base+", "+exp.rl+", "+exp.rr+")", "");
		}
	}

	@Override
	public Pair<String, String> visit(EnforceBitExp exp) {
		if(exp.v.lab == Label.Pub) {
			return new Pair<String, String>(exp.v.name, "");
		} else {
			return new Pair<String, String>("intLib.enforceBits("+exp.v+", "+exp.bits+")", "");
		}
	}

	@Override
	public String visit(RangeAssign rangeAssign) {
		if(rangeAssign.lab == Label.Pub) {
			// TODO easy, but not implemented yet.
			throw new RuntimeException("Not supported yet.");
		} else {
			if(rangeAssign.right == null) {
				return indent(indent)+rangeAssign.name+"["+rangeAssign.left+"]="+rangeAssign.value+";\n";
			} else {
				return indent(indent)+"System.arraycopy("+rangeAssign.value+", 0, "+rangeAssign.name+", "+rangeAssign.left
						+", ("+rangeAssign.right+")-("+rangeAssign.left+"));\n";
			}
		}
	}

	public String targetName(Type type) {
		if(type instanceof IntType) {
			return "BoxedInt<"+dataType+">";
		} else if(type instanceof FloatType) {
			return "BoxedFloat<"+dataType+">";
		} else if(type instanceof RndType) {
			return "BoxedRnd<"+dataType+">";
		} else return visit(type);
	}

	@Override
	public Pair<String, String> visit(BoxNullExp exp) {
		if(exp.var.type instanceof NullType) {
			throw new RuntimeException("Shouldn't visit here.");
		} else {
			String value = exp.var.name;
			if(exp.var.lab == Label.Pub) {
				if(exp.var.type instanceof FloatType)
					value = "new BoxedFloat<"+dataType+">(env, floatLib.inputOfAlice("+value+", " + exp.var.getBits() +"))";
				else if(exp.var.type instanceof IntType)
					value = "new BoxedInt<"+dataType+">(env, env.inputOfAlice(Utils.fromInt("+value+", " + exp.var.getBits() +")))";
				else if(exp.var.type instanceof RndType)
					value = "new BoxedRnd<"+dataType+">(env, env.inputOfAlice(Utils.fromInt("+value+", " + exp.var.getBits() +")))";
			} else {
				if(exp.var.type instanceof FloatType)
					value = "new BoxedFloat<"+dataType+">(env, "+value+")";
				else if(exp.var.type instanceof IntType)
					value = "new BoxedInt<"+dataType+">(env, "+value+")";
				else if(exp.var.type instanceof RndType)
					value = "new BoxedRnd<"+dataType+">(env, "+value+")";
			}
			return new Pair<String, String>(
					"new NullableType<"+dataType+", "+targetName(exp.var.type)+">(env, "+value+", env.inputOfAlice(true))", 
					"");
		}
	}

	@Override
	public Pair<String, String> visit(CheckNullExp exp) {
		return new Pair<String, String>(
				exp.checkEq ? exp.var+".isNull" : "intLib.not("+exp.var+".isNull)",
				"");
	}

	@Override
	public Pair<String, String> visit(GetValueExp exp) {
		String t = null;
		switch(exp.way) {
		case Shallow:
			t = exp.var.name+".muxFake()";
			break;
		case GetValue:
			t = exp.var.name+".value";
			if(((DummyType)exp.var.type).type.rawType())
				t += ".value";
			break;
		case PureRandom:
			t = exp.var.name+".getFake()";
		}
		return new Pair<String, String>(t, "");
	}

	@Override
	public Pair<String, String> visit(NullExp exp) {
		throw new RuntimeException("Shouldn't visit here.");
	}

	@Override
	public String visit(UsingBlock ret) {
		StringBuffer sb = new StringBuffer();
		sb.append(indent(indent) + "{");
		indent ++;
		sb.append(visit(ret.code));
		indent --;
		sb.append(indent(indent) + "}");
		return sb.toString();
	}

}

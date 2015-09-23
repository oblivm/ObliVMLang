/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;


public abstract class IRVisitor<T1, T2> {
	
	public T1 visit(IRCode ast) {
		if(ast instanceof ArrayAssign) {
			return visit((ArrayAssign)ast);
		} else if(ast instanceof Assign) {
			return visit((Assign)ast);
		} else if(ast instanceof RangeAssign) {
			return visit((RangeAssign)ast);
		} else if(ast instanceof RecordAssign) {
			return visit((RecordAssign)ast);
		} else if(ast instanceof ReverseRecordAssign) {
			return visit((ReverseRecordAssign)ast);
		} else if(ast instanceof If) {
			return visit((If)ast);
		} else if(ast instanceof Ret) {
			return visit((Ret)ast);
		} else if(ast instanceof Seq) {
			return visit((Seq)ast);
		} else if(ast instanceof Skip) {
			return visit((Skip)ast);
		} else if(ast instanceof While) {
			return visit((While)ast);
		} else if(ast instanceof UsingBlock) {
			return visit((UsingBlock)ast);
		} else if(ast instanceof Debug) {
			return visit((Debug)ast);
		} else
			throw new RuntimeException("Unknown SCVM statement!");
	}
	
	public T2 visit(Expression exp) {
		if(exp instanceof BopExp) {
			return visit((BopExp)exp);
		} else if(exp instanceof ArrayExp) {
			return visit((ArrayExp)exp);
		} else if(exp instanceof UnaryOpExp) {
			return visit((UnaryOpExp)exp);
		} else if(exp instanceof ConstExp) {
			return visit((ConstExp)exp);
		} else if(exp instanceof NativeFuncCallExp) {
			return visit((NativeFuncCallExp)exp);
		} else if(exp instanceof FuncCallExp) {
			return visit((FuncCallExp)exp);
		} else if(exp instanceof LocalFuncCallExp) {
			return visit((LocalFuncCallExp)exp);
		} else if(exp instanceof RecExp) {
			return visit((RecExp)exp);
		} else if(exp instanceof MuxExp) {
			return visit((MuxExp)exp);
		} else if(exp instanceof RopExp) {
			return visit((RopExp)exp);
		} else if(exp instanceof SopExp) {
			return visit((SopExp)exp);
		} else if(exp instanceof NewObjExp) {
			return visit((NewObjExp)exp);
		} else if(exp instanceof VarExp) {
			return visit((VarExp)exp);
		} else if(exp instanceof LogExp) {
			return visit((LogExp)exp);
		} else if(exp instanceof SizeofExp) {
			return visit((SizeofExp)exp);
		} else if(exp instanceof RangeExp) {
			return visit((RangeExp)exp);
		} else if(exp instanceof EnforceBitExp) {
			return visit((EnforceBitExp)exp);
		} else if(exp instanceof BoxNullExp) {
			return visit((BoxNullExp)exp);
		} else if(exp instanceof CheckNullExp) {
			return visit((CheckNullExp)exp);
		} else if(exp instanceof GetValueExp) {
			return visit((GetValueExp)exp);
		} else if(exp instanceof NullExp) {
			return visit((NullExp)exp);
		} else
			throw new RuntimeException("Unknown SCVM expression!");
	}
	
	public abstract T2 visit(ArrayExp exp);
	public abstract T2 visit(BopExp exp);
	public abstract T2 visit(UnaryOpExp exp);
	public abstract T2 visit(ConstExp exp);
	public abstract T2 visit(MuxExp exp);
	public abstract T2 visit(RopExp exp);
	public abstract T2 visit(SopExp exp);
	public abstract T2 visit(VarExp exp);
	public abstract T2 visit(RecExp exp);
	public abstract T2 visit(LogExp exp);
	public abstract T2 visit(SizeofExp exp);
	public abstract T2 visit(RangeExp exp);
	public abstract T2 visit(EnforceBitExp exp);
	public abstract T2 visit(FuncCallExp exp);
	public abstract T2 visit(LocalFuncCallExp exp);
	public abstract T2 visit(NewObjExp exp);
	public abstract T2 visit(NativeFuncCallExp exp);
	public abstract T2 visit(BoxNullExp exp);
	public abstract T2 visit(CheckNullExp exp);
	public abstract T2 visit(GetValueExp exp);
	public abstract T2 visit(NullExp exp);
	
	public abstract T1 visit(ArrayAssign arrayAssign);
	public abstract T1 visit(Assign assign);
	public abstract T1 visit(RangeAssign rangeAssign);
	public abstract T1 visit(If ifStmt);
	public abstract T1 visit(Seq seq);
	public abstract T1 visit(Skip skip);
	public abstract T1 visit(While whileStmt);
	public abstract T1 visit(RecordAssign assign);
	public abstract T1 visit(ReverseRecordAssign assign);
	public abstract T1 visit(Ret ret);
	public abstract T1 visit(UsingBlock ret);
	public abstract T1 visit(Debug ret);
}

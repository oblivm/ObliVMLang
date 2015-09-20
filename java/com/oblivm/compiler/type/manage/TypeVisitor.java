/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public abstract class TypeVisitor<T> {
	public abstract T visit(ArrayType type);
	
	public abstract T visit(FloatType type);
	
	public abstract T visit(FunctionType type);

	public abstract T visit(IntType type);

	public abstract T visit(RecordType type);

	public abstract T visit(RndType type);

	public abstract T visit(VariableType type);

	public abstract T visit(VoidType type);

	public abstract T visit(NativeType type);
	
	public abstract T visit(NullType type);
	
	public abstract T visit(DummyType type);
	
	public T visit(Type type) {
		if(type instanceof ArrayType) {
			return visit((ArrayType)type);
		} else if(type instanceof FloatType) {
			return visit((FloatType)type);
		} else if(type instanceof FunctionType) {
			return visit((FunctionType)type);
		} else if(type instanceof IntType) {
			return visit((IntType)type);
		} else if(type instanceof RecordType) {
			return visit((RecordType)type);
		} else if(type instanceof RndType) {
			return visit((RndType)type);
		} else if(type instanceof VariableType) {
			return visit((VariableType)type);
		} else if(type instanceof VoidType) {
			return visit((VoidType)type);
		} else if(type instanceof NativeType) {
			return visit((NativeType)type);
		} else if(type instanceof NullType) {
			return visit((NullType)type);
		} else if(type instanceof DummyType) {
			return visit((DummyType)type);
		} else 
			throw new RuntimeException("Unknown type");
	}
}

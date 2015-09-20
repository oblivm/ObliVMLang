package com.oblivm.compiler.ast;

public class Position {
	public int line;
	public int column;
	
	public Position(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	public String toString() {
		return "("+line+", "+column+")";
	}
}

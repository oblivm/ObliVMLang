package com.oblivm.compiler.log;

import java.io.PrintStream;

import com.oblivm.compiler.ast.Position;

public abstract class Log {
	public PrintStream ps;
	
	public abstract String tag();
	
	public boolean isOn = true;
	
	public void turnOn() {
		isOn = true;
	}
	
	public void turnOff() {
		isOn = false;
	}
	
	public Log(PrintStream ps) {
		this.ps = ps;
	}
	
	public void log(Position pos, String msg) {
		log(pos.toString()+": "+msg);
	}
	
	public void log(String msg) {
		if(isOn) {
			ps.println("["+tag()+"]\t"+msg);
		}
	}
}

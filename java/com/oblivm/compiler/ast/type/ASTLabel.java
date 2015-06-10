/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

public class ASTLabel {
	public static final ASTLabel Pub = new ASTLabel("public", 0);
	public static final ASTLabel Alice = new ASTLabel("alice", 1);
	public static final ASTLabel Bob = new ASTLabel("bob", 2);
	public static final ASTLabel Secure = new ASTLabel("secure", 3);
	
	private String name;
	private int id;
	
	public static int getLabelNumber() {
		return 6;
	}
	
	private ASTLabel() {};
	private ASTLabel(String name, int id) { this.name = name; this.id = id; };
	
	public String toString() {
		return name;
	}
	
	public boolean less(ASTLabel lab) {
		if(this == Pub || lab == Secure || this == lab)
			return true;
		return false;
	}
	
	public int getId() {
		return id;
	}
	
	/***
	 * this cup lab
	 * @param lab
	 * @return
	 */
	public ASTLabel meet(ASTLabel lab) {
		if(this == ASTLabel.Pub)
			return lab;
		if(lab == ASTLabel.Pub)
			return this;
		if(this == lab)
			return this;
		return ASTLabel.Secure;
	}

	/***
	 * this cap lab
	 * @param label
	 * @return
	 */
	public ASTLabel join(ASTLabel lab) {
		if(this == ASTLabel.Secure)
			 return lab;
		if(lab == ASTLabel.Secure)
			return this;
		if(lab == this)
			return this;
		return Pub;
	}
}

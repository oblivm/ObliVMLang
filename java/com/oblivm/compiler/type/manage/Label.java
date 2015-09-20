/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import com.oblivm.compiler.ast.type.ASTLabel;


public class Label {
	public static final Label Pub = new Label("public", 0);
	public static final Label Alice = new Label("alice", 1);
	public static final Label Bob = new Label("bob", 2);
	public static final Label Secure = new Label("secure", 3);
//	public static final Label Affine = new Label("affine", 4);
//	public static final Label Infer = new Label("", 5);
	
	private String name;
	private int id;
	
	public static Label get(ASTLabel lab) {
		if(lab == ASTLabel.Pub)
			return Pub;
		else if(lab == ASTLabel.Alice)
			return Alice;
		else if(lab == ASTLabel.Bob)
			return Bob;
		else if(lab == ASTLabel.Secure)
			return Secure;
		else
			throw new RuntimeException("Unknown label.");
	}
	
	public static int getLabelNumber() {
		return 6;
	}
	
	private Label() {};
	private Label(String name, int id) { this.name = name; this.id = id; };
	
	public String toString() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public Label meet(Label lab) {
		if(lab == Label.Pub)
			return this;
		if(this == Label.Pub)
			return lab;
		return Label.Secure;
	}
}

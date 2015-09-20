/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend.flexsc;


public class Config {
	public String packageName = "compiledlib.libs";
	public static String[] importPackage = new String[]{
		"java.util.Arrays",
		"java.util.Random",
		"java.lang.reflect.Array",
		"java.util.BitSet", 
		"java.security.SecureRandom",
		"com.oblivm.backend.oram.SecureArray",
		"com.oblivm.backend.oram.CircuitOram",
		"com.oblivm.backend.flexsc.Mode",
		"com.oblivm.backend.flexsc.Party",
		"com.oblivm.backend.flexsc.CompEnv",
		"com.oblivm.backend.circuits.arithmetic.IntegerLib",
//		"circuits.IntegerLib",
		"com.oblivm.backend.circuits.arithmetic.FloatLib",
		"com.oblivm.backend.util.Utils",
		"com.oblivm.backend.gc.regular.GCEva",
		"com.oblivm.backend.gc.regular.GCGen",
//		"gc.GCEva",
//		"gc.GCGen",
		"com.oblivm.backend.gc.GCSignal",
//		"flexsc.scvm.*",
		"com.oblivm.backend.flexsc.IWritable",
		"com.oblivm.backend.lang.inter.*",
		"com.oblivm.backend.flexsc.Comparator"};

	public String basePath = "../../git/FlexSC/src/"; 
	public String path = basePath + packageName.replace(".", "/");
	
	public static boolean useTemplate = true;
	
	public static String[] types = new String[] {
		"CompEnv<@dataType>"
		,"IntegerLib<@dataType>"
		,"FloatLib<@dataType>"
		,};
	
	public static String phantomVariable = "__isPhantom";
	
	public static String[] fields = new String[] {
		"env"
		,"intLib"
		,"floatLib"
	};

	public void setPackageName(String packageName) {
		this.packageName = packageName;
		path = basePath + packageName.replace(".", "/");
	}
}

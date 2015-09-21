/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend;

import com.oblivm.compiler.type.manage.TypeManager;

public interface ICodeGenerator {
	public void codeGen(TypeManager tm, String packageName, 
			String shellFolder, 
			boolean countMode,
			int port);
}

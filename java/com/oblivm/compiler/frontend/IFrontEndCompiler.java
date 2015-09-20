/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend;

import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.type.manage.TypeManager;

public interface IFrontEndCompiler {
	public TypeManager compile(ASTProgram program);
}

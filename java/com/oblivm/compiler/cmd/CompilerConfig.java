/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.cmd;

import com.oblivm.compiler.backend.ICodeGenerator;
import com.oblivm.compiler.backend.flexsc.FlexSCCodeGenerator;
import com.oblivm.compiler.frontend.FrontendCompiler;
import com.oblivm.compiler.frontend.IFrontEndCompiler;

public class CompilerConfig {
	public String outputDirectory = "./bin";
	public String backend = "flexsc";
	
	public IFrontEndCompiler getFrontend() {
		return new FrontendCompiler();
	}
	
	public ICodeGenerator getCodeGen() {
		if(backend.equals("flexsc")) {
			FlexSCCodeGenerator codeGen = new FlexSCCodeGenerator();
			codeGen.config = new com.oblivm.compiler.backend.flexsc.Config();
			codeGen.config.basePath = this.outputDirectory;
			return codeGen;
		}
		throw new RuntimeException("Unknown backend");
	}
}

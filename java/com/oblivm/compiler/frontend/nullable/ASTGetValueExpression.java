/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend.nullable;

import com.oblivm.compiler.ast.expr.ASTExpression;

/***
 * Get Value will return a value of type T from a value of type Nullable<T>.
 * 
 * If way is Shallow, then its value may or may not be null. The returned value will be either its value or a randomly generated number;
 * 
 * If way is GetValue, then its value must not be null, and it is returned;
 * 
 * If way is PureRandom, then its value must be null, and a randomly generated number will be returned.
 * 
 * @author Chang Liu
 *
 */
public class ASTGetValueExpression extends ASTExpression {

	public enum HandleWay {
		Shallow, GetValue, PureRandom;
	}
	
	public HandleWay way = HandleWay.Shallow;
	
	public ASTExpression exp;
	
	public ASTGetValueExpression(HandleWay toShaddow, ASTExpression exp) {
		this.way = toShaddow;
		this.exp = exp;
	}
	
	public String toString() {
		switch (way) {
		case Shallow:
			return "GetValue("+exp+")";
		case GetValue:
			return "("+exp+").value";
		case PureRandom:
			return "random("+exp+")";
		}
		return exp.toString();
	}
	
	@Override
	public int level() {
		return 100;
	}

	public ASTGetValueExpression cloneInternal() {
		return new ASTGetValueExpression(way, exp.clone());
	}
}

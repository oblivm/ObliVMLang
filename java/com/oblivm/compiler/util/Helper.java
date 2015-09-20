/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.util;

import com.oblivm.compiler.ast.expr.ASTBinaryExpression.BOP;
import com.oblivm.compiler.ast.expr.ASTBinaryPredicate.REL_OP;

import java.util.*;

public class Helper {
	public static boolean eval(int v1, int v2, REL_OP op) {
		if(op == REL_OP.EQ)
			return v1 == v2;
		else if(op == REL_OP.NEQ)
			return v1 != v2;
		else if(op == REL_OP.GT)
			return v1 > v2;
		else if(op == REL_OP.GET)
			return v1 >= v2;
		else if(op == REL_OP.LT)
			return v1 < v2;
		else if(op == REL_OP.LET)
			return v1 <= v2;
		else 
			return false;
	}
	
	public static int eval(int v1, int v2, BOP op) {
		if(op == BOP.ADD)
			return v1 + v2;
		if(op == BOP.SUB)
			return v1 - v2;
		if(op == BOP.MUL)
			return v1 * v2;
		if(op == BOP.DIV)
			return v1 / v2;
		if(op == BOP.MOD)
			return v1 % v2;
		else
			throw new RuntimeException("Unknown Operator");
		
	}
	
	public static int countBits(int val) {
		int c = 0;
		while(val > 0)
		{
			c++;
			val >>= 1;
		}
		if(c == 0) return 1;
		else return c;
	}
	
	public static int parseInt(String str) {
		if(str.startsWith("0x")) {
			// hex
			int ret = 0;
			for(int i=2; i<str.length(); ++i) {
				char c = str.charAt(i);
				if(c >= '0' && c <= '9')
					ret = ret * 16 + c - '0';
				else if (c >= 'a' && c <= 'f')
					ret = ret * 16 + c - 'a' + 10;
				else if (c >= 'A' && c <= 'F')
					ret = ret * 16 + c - 'A';
				else
					throw new RuntimeException("For input string : "+str);
			}
			return ret;
		} else if (str.startsWith("0")) {
			// oct
			int ret = 0;
			for(int i=1; i<str.length(); ++i) {
				char c = str.charAt(i);
				if(c < '0' || c > '9')
					throw new RuntimeException("For input string : "+str);
				ret = ret * 8 + c - '0';
			}
			return ret;
		} else {
			return Integer.decode(str);
		}
	}
	
	public static<T> T last(List<T> list) {
		return list.get(list.size() - 1);
	}
}

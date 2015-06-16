/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.source;

import java.util.HashMap;
import java.util.Map;

public class ResourceBudget {
	public Map<String, Integer> budgets; // variable -> budget
	
	public ResourceBudget() {
		budgets = new HashMap<String, Integer>();
	}
	
	public ResourceBudget(Map<String, Integer> budget) {
		this.budgets = budget;
	}
	
	public boolean canUse(String var) {
		return budgets.containsKey(var) && budgets.get(var) > 0;
	}
	
	public boolean use(String var) {
		if(!canUse(var))
			return false;
		Integer val = budgets.get(var);
		if(val == Integer.MAX_VALUE)
			return true;
		budgets.put(var, val - 1);
		return true;
	}
	
	public void addAffine(String var) {
		budgets.put(var, 1);
	}
	
	public void addStar(String var) {
		budgets.put(var, Integer.MAX_VALUE);
	}
	
	public ResourceBudget join(ResourceBudget a) {
		Map<String, Integer> budget = new HashMap<String, Integer>();
		for(Map.Entry<String, Integer> entry : budgets.entrySet()) {
			if(a.budgets.containsKey(entry.getKey())) {
			    int ax = entry.getValue();
			    int ay = a.budgets.get(entry.getKey());
				budget.put(entry.getKey(), ax < ay ? ax : ay);
			}
		}
		return new ResourceBudget(budget);
	}
	
	public ResourceBudget clone() {
		Map<String, Integer> budget = new HashMap<String, Integer>();
		for(Map.Entry<String, Integer> ent : this.budgets.entrySet()) {
			budget.put(ent.getKey(), ent.getValue());
		}
		return new ResourceBudget(budget);
	}
	
	public boolean equal(ResourceBudget e) {
		for(Map.Entry<String, Integer> ent : this.budgets.entrySet()) {
			if(!e.budgets.containsKey(ent.getKey()) 
					|| e.budgets.get(ent.getKey()) - ent.getValue() != 0) {
				return false;
			}
		}
		return this.budgets.size() == e.budgets.size();
	}
}

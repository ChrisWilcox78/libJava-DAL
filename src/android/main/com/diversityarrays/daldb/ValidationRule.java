/*
 * dalclient library - provides utilities to assist in using KDDart-DAL servers
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.daldb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

public abstract class ValidationRule {
	
	private static final String DEFAULT_BOOLEX_VARIABLE_NAME = "x";
	
	// Helper method for unit tests
	public static boolean getMightUseJavascript(String rule) {
		return rule.toLowerCase().startsWith("boolex(");
	}

	static public boolean USE_PUTHICK_HACK = false;

//	static private final String[] ANDROID_TESTS = {
//		"java.runtime.name=android runtime",
//		"java.vendor.url=http://www.android.com/",
//		"java.vm.vendor.url=http://www.android.com/",
//		"java.home=/system",
//		"java.vm.name=Dalvik",
//		"java.runtime.name=Android Runtime",
//		"java.specification.vendor=The Android Project",
//		"java.vm.specification.vendor=The Android Project",
//		"java.vm.vendor=The Android Project",
//		"android.vm.dexfile=true",
//		"java.specification.name=Dalvik Core Library",
//		"java.vendor=The Android Project",
//		"java.vm.specification.name=Dalvik Virtual Machine Specification",
//	};
	
	static public Boolean USE_JAVASCRIPT = null;
	
	static private boolean shouldUseJavascript() {
		if (USE_JAVASCRIPT==null) {
			try {
				new ScriptEngineManager().getEngineByName("JavaScript");
				USE_JAVASCRIPT = true;
			}
			catch (Exception e) {
				USE_JAVASCRIPT = false;
			}
//			ANDROID = ! Boolean.parseBoolean(System.getProperty("android.vm.dexfile"));
			System.out.println(ValidationRule.class.getName()+".USE_JAVASCRIPT="+USE_JAVASCRIPT);
		}
		return USE_JAVASCRIPT.booleanValue();
	}
	
	static public ValidationRule create(String rule) throws InvalidRuleException {
		return create(rule, DEFAULT_BOOLEX_VARIABLE_NAME);
	}

	static public ValidationRule create(String rule, String boolexVariableName) throws InvalidRuleException {

		ValidationRule result;
		
		Pattern validationRulePattern = Pattern.compile("(BOOLEX|REGEX)\\((.*)\\)$");

		Matcher m = validationRulePattern.matcher(rule);
		if (! m.matches()) {
			throw new InvalidRuleException("Not BOOLEX or REGEX: "+rule);
		}
		
		String ruleType = m.group(1);
		String expression = m.group(2);
		
		if ("BOOLEX".equalsIgnoreCase(ruleType)) {
			if (shouldUseJavascript()) {
				result = new JavascriptBoolex(expression, boolexVariableName==null ? DEFAULT_BOOLEX_VARIABLE_NAME : boolexVariableName);
			}
			else {
				result = new ParsiiBoolex(expression, boolexVariableName==null ? DEFAULT_BOOLEX_VARIABLE_NAME : boolexVariableName);
			}
		}
		else if ("REGEX".equalsIgnoreCase(ruleType)) {
			result = new Regex(expression);
		}
		else {
			throw new RuntimeException("Should never happen: ruleType="+ruleType);
		}
		
		return result;
	}
	
	protected final String expression;
	protected final String variableName;

	private ValidationRule(String expr, String varname) {
		this.expression = expr;
		this.variableName = varname;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public abstract boolean evaluate(String input);
	
	public List<Boolean> evaluate(List<String> inputs) {
		List<Boolean> result = new ArrayList<Boolean>(inputs.size());
		for (String input : inputs) {
			result.add(evaluate(input));
		}
		return result;
	}
	
	public boolean[] evaluate(String ... inputs) {
		boolean[] result = new boolean[inputs.length];
		int idx = 0;
		for (String input : inputs) {
			result[idx++] = evaluate(input);
		}
		return result;
	}
	
	
	static class JavascriptBoolex extends ValidationRule {

		private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

		JavascriptBoolex(String expr, String varname) throws InvalidRuleException {
			super(expr, varname);
			
			// Before returning, make sure that expression is a boolean expression
			engine.put(variableName, new Integer(0));
			try {
				Object value = engine.eval(expression);
				if (! (value instanceof Boolean)) {
					throw new InvalidRuleException("Non-boolean expression in BOOLEX("+expression+")");
				}
			} catch (ScriptException e) {
				throw new InvalidRuleException(e);
			}
		}
		
		@Override
		public boolean isBoolex() {
			return true;
		}

		@Override
		public boolean evaluate(String input) {
			boolean result = false;
			
			engine.put(variableName, input);
			try {
				Object value = engine.eval(expression);
				if (value instanceof Boolean) {
					result = ((Boolean) value).booleanValue();
				}
			} catch (ScriptException e) {
				System.err.println(this+" for '"+input+"' : "+e.getMessage());
				// bummer: 
			}
			return result;
		}
		
		@Override
		public String toString() {
			return "BOOLEX("+expression+")";
		}
	}

	static class ParsiiBoolex extends ValidationRule {

		private final String expr;
		private final Expression expression;
		private final Scope scope;
		private final Variable variable;
		
		ParsiiBoolex(String expr, String varname) throws InvalidRuleException {
			super(expr, varname);
			
			this.expr = expr;
			scope = Scope.create();
			variable = scope.getVariable(varname);

			// Before returning, make sure that expression is a boolean expression
			try {
				variable.setValue(Integer.MAX_VALUE);
				expression = Parser.parse(expr, scope);
				double value = expression.evaluate();
				if (value!=0 && value!=1) {
					throw new InvalidRuleException("BOOLEX("+expression+") must evaluate to 0 or 1");
				}
			} catch (ParseException e) {
				throw new InvalidRuleException(e);
			}
		}

		@Override
		public boolean isBoolex() {
			return true;
		}
		
		@Override
		public boolean evaluate(String input) {
			boolean result = false;
	
			
			try {
				variable.setValue(Double.parseDouble(input));

				double value = expression.evaluate();
				
				result = value != 0;
			} catch (NumberFormatException e) {
				System.err.println(this+" for '"+input+"' : "+e.getMessage());
			}
			return result;
		}
		
		@Override
		public String toString() {
			return "BOOLEX("+expr+")";
		}
	}
	
	static private class Regex extends ValidationRule {

		/**
		 * For some reason Puthick used  [] for  REGEX( [Early|Medium|Late] )
		 * instead of parentheses. This fixes that issue (but only for the outermost set)
		 * for the test data in some of the test databases.
		 * @param expression
		 * @return
		 */
		static private String fixExpression(String expression) {
			String result = expression;
			if (expression.startsWith("[") && expression.endsWith("]")) {
				result = expression.substring(1, expression.length() - 1);
			}
			return result;
		}

		private final Pattern regex;

		Regex(String expr) throws InvalidRuleException {
			super(expr, null /* no variable name for REGEX */);

			try {
				if (USE_PUTHICK_HACK) {
					regex = Pattern.compile(fixExpression(expression));
				}
				else {
					regex = Pattern.compile(expression);
				}
			} catch (PatternSyntaxException e) {
				throw new InvalidRuleException("Invalid pattern: REGEX("+expression+") : "+e.getMessage());
			}
		}

		@Override
		public boolean isBoolex() {
			return false;
		}
		
		@Override
		public boolean evaluate(String input) {
			return regex.matcher(input).matches();
		}
		
		@Override
		public String toString() {
			return "REGEX("+expression+")";
		}
	}

	abstract public boolean isBoolex();
	
	public boolean isRegex() {
		return ! isBoolex();
	}

}

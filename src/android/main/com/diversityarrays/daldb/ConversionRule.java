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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.collections15.Transformer;

public abstract class ConversionRule<I,O> {

	/**
	 * This is the default variable name for ConversionRule expressions.
	 */
	public static final String DEFAULT_VARIABLE_NAME = "x";

	static public Transformer<Object,Object> IDENTITY_TRANSFORM = new Transformer<Object, Object>() {
		@Override
		public Object transform(Object in) {
			return in;
		}
	};
	
	static public ConversionRule<Object,Object> create(String rule) throws InvalidRuleException {
		return create(rule, DEFAULT_VARIABLE_NAME, IDENTITY_TRANSFORM);
	}

	static public <I,O> ConversionRule<I,O>  create(String rule, Transformer<Object,O> resultConverter) throws InvalidRuleException {
		return create(rule, DEFAULT_VARIABLE_NAME, resultConverter);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public <I,O> ConversionRule<I,O> create(String rule, String variableName, Transformer<Object,O> resultConverter) throws InvalidRuleException {

		ConversionRule<I,O>  result;

		if (rule==null || rule.trim().isEmpty()) {
			result = new NullExpression(resultConverter);
		}
		else {
			Pattern rulePattern = Pattern.compile("(EXPR|EXTERNAL)\\((.*)\\)$");

			Matcher m = rulePattern.matcher(rule);
			if (! m.matches()) {
				throw new InvalidRuleException("Not EXPR or EXTERNAL: "+rule);
			}

			String ruleType = m.group(1);
			String expression = m.group(2);

			if ("EXPR".equalsIgnoreCase(ruleType)) {
				result = new Expr<I,O>(expression, variableName==null ? DEFAULT_VARIABLE_NAME : variableName, resultConverter);
			}
			else if ("EXTERNAL".equalsIgnoreCase(ruleType)) {
				result = new External(expression);
			}
			else {
				throw new RuntimeException("Should never happen: ruleType="+ruleType);
			}
		}
		return result;
	}

	protected final String expression;
	protected final String variableName;
	private ConversionRule(String code, String varname) {
		this.expression = code;
		this.variableName = varname;
	}
	
	public abstract O evaluate(I input) throws InvalidRuleException;

	static private class NullExpression<I,O>  extends ConversionRule<I,O> {
		
		private final Transformer<Object,O> resultConverter;
		
		NullExpression(Transformer<Object,O> resultConverter) {
			super(null, null);
			this.resultConverter = resultConverter;
		}
		
		@Override
		public O evaluate(I input) {
			return resultConverter.transform(input);
		}		
		
		@Override
		public String toString() {
			return "IDENTITY()";
		}
	}
	
	static public class Expr<I,O> extends ConversionRule<I,O> {
		
		private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
		private final Transformer<Object,O> resultConverter;
		
		public Expr(String code, String varname, Transformer<Object,O> resultConverter) throws InvalidRuleException {
			super(code, varname);

			this.resultConverter = resultConverter;
			
			engine.put(variableName, "0");
			try {
				engine.eval(expression);
			} catch (ScriptException e) {
				throw new InvalidRuleException(e);
			}
		}
		
		@Override
		public O evaluate(I input) {
			Object result = null;
			
			engine.put(variableName, input);
			try {
				result = engine.eval(expression);
			} catch (ScriptException e) {
				System.err.println(this+" for '"+input+"' : "+e.getMessage());
				// bummer: 
			}
			return resultConverter.transform(result);
		}
		
		@Override
		public String toString() {
			return "EXPR("+expression+")";
		}
	}

	static class External<I,O> extends ConversionRule<I,O> {
		
		External(String code) {
			super(code, null);
		}
		
		@Override
		public O evaluate(I input) throws InvalidRuleException {
			throw new InvalidRuleException("Attempt to execute "+this);
		}
		
		@Override
		public String toString() {
			return "EXTERNAL("+expression+")";
		}
	}

}

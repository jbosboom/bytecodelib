/*
 * Copyright (c) 2013-2014 Massachusetts Institute of Technology
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.mit.streamjit.util.bytecode.insts;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import edu.mit.streamjit.util.bytecode.Method;
import edu.mit.streamjit.util.bytecode.Value;
import edu.mit.streamjit.util.bytecode.types.MethodType;
import edu.mit.streamjit.util.bytecode.types.PrimitiveType;
import edu.mit.streamjit.util.bytecode.types.RegularType;
import edu.mit.streamjit.util.bytecode.types.ReturnType;
import edu.mit.streamjit.util.bytecode.types.VoidType;

/**
 * A method call.  All types of bytecoded calls (i.e., not invokedynamic) use
 * this instruction; the opcode to generate is determined by the method being
 * called and the relationship between the instruction's parent class and the
 * method's parent class.
 *
 * TODO: this needs to track the class hierarchy so it can change methods if we
 * add or remove an override.  (Not really a problem for StreamJIT's purposes
 * but annoyingly un-general.)
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/13/2013
 */
public final class CallInst extends Instruction {
	private final MethodType methodType;
	public CallInst(Method m) {
		this(m, m.getType());
	}
	public CallInst(Method m, Value... arguments) {
		this(m, m.getType(), arguments);
	}
	public CallInst(Method m, MethodType methodType, Value... arguments) {
		super(methodType.getReturnType(), 1 + methodType.getParameterTypes().size());
		checkArgument(m.isSignaturePolymorphic() || methodType.equals(m.getType()));
		this.methodType = methodType;
		setOperand(0, m);
		for (int i = 0; i < arguments.length; ++i)
			setArgument(i, arguments[i]);
	}
	@Override
	public ReturnType getType() {
		return (ReturnType)super.getType();
	}
	public Method getMethod() {
		return (Method)getOperand(0);
	}
	public void setMethod(Method m) {
		setOperand(0, m);
	}
	public Value getArgument(int i) {
		return getOperand(i+1);
	}
	public void setArgument(int i, Value v) {
		setOperand(i+1, v);
	}
//	public void addArgument(int i, Value v) {
//		checkState(getMethod().isSignaturePolymorphic(), "can't add arguments to non-signature-polymorphic method %s", getMethod());
//		super.addOperand(i + 1, v);
//	}
//	public void removeArgument(int i, Value v) {
//		checkState(getMethod().isSignaturePolymorphic(), "can't remove arguments to non-signature-polymorphic method %s", getMethod());
//		super.removeOperand(i + 1);
//	}
	public Iterable<Value> arguments() {
		return Iterables.skip(operands(), 1);
	}

	public String callDescriptor() {
		MethodType type = methodType;
		if (getMethod().isConstructor())
			type = type.withReturnType(type.getTypeFactory().getVoidType());
		if (getMethod().hasReceiver())
			type = type.dropFirstArgument();
		return type.getDescriptor();
	}

	@Override
	public CallInst clone(Function<Value, Value> operandMap) {
		Value[] arguments = new Value[Iterables.size(arguments())];
		for (int i = 0; i < arguments.length; ++i)
			arguments[i] = operandMap.apply(getArgument(i));
		Method newMethod = (Method)operandMap.apply(getMethod());
		MethodType newMethodType = newMethod.isSignaturePolymorphic() ? methodType : newMethod.getType();
		return new CallInst(newMethod, newMethodType, arguments);
	}

	@Override
	protected void checkOperand(int i, Value v) {
		if (i == 0)
			checkArgument(v instanceof Method);
		else {
			RegularType paramType = methodType.getParameterTypes().get(i-1);
			PrimitiveType intType = paramType.getTypeFactory().getPrimitiveType(int.class);
			//Due to the JVM's type system not distinguishing types smaller than
			//int, we can implicitly convert to int then to the parameter type.
			//Otherwise, we need a subtype match.
			if (!(v.getType().isSubtypeOf(intType) && paramType.isSubtypeOf(intType)))
				checkArgument(v.getType().isSubtypeOf(paramType),
						"cannot assign %s (%s) to parameter type %s",
						v, v.getType(), paramType);
		}
		super.checkOperand(i, v);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getName());
		if (getType() instanceof VoidType)
			sb.append(": ");
		else
			sb.append(" (").append(getType()).append(") = ");
		sb.append("call ").append(getMethod().getParent().getName()).append("#").append(getMethod().getName());
		sb.append("(");
		Joiner.on(", ").appendTo(sb, FluentIterable.from(arguments()).transform(new Function<Value, String>() {
			@Override
			public String apply(Value input) {
				return input.getName();
			}
		}));
		sb.append(")");
		return sb.toString();
	}
}

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
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import edu.mit.streamjit.util.bytecode.Value;
import edu.mit.streamjit.util.bytecode.types.PrimitiveType;

/**
 * A binary mathematical operation.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/15/2013
 */
public final class BinaryInst extends Instruction {
	public enum Operation {
		ADD("int", "long", "float", "double"),
		SUB("int", "long", "float", "double"),
		MUL("int", "long", "float", "double"),
		DIV("int", "long", "float", "double"),
		REM("int", "long", "float", "double"),
		SHL("int", "long"),
		SHR("int", "long"),
		USHR("int", "long"),
		AND("int", "long"),
		OR("int", "long"),
		XOR("int", "long"),
		CMP("long", "float", "double"),
		CMPG("float", "double");
		private final ImmutableList<String> types;
		private Operation(String... types) {
			this.types = ImmutableList.copyOf(types);
		}
		public ImmutableList<String> applicableTypes() {
			return types;
		}
	}

	private final Operation operation;
	public BinaryInst(Value left, Operation op, Value right) {
		super(computeType(left, op, right), 2);
		if (op == Operation.CMP || op == Operation.CMPG)
			checkArgument(op.applicableTypes().contains(left.getType().toString()) &&
					op.applicableTypes().contains(left.getType().toString()),
					"%s %s %s", left.getType(), op, right.getType());
		else
			checkArgument(op.applicableTypes().contains(getType().toString()), "%s %s", op, getType());
		setOperand(0, left);
		setOperand(1, right);
		this.operation = op;
	}

	@Override
	public PrimitiveType getType() {
		return (PrimitiveType)super.getType();
	}

	public Operation getOperation() {
		return operation;
	}

	@Override
	public BinaryInst clone(Function<Value, Value> operandMap) {
		return new BinaryInst(operandMap.apply(getOperand(0)), operation, operandMap.apply(getOperand(1)));
	}

	private static PrimitiveType computeType(Value left, Operation operation, Value right) {
		PrimitiveType intType = left.getType().getTypeFactory().getPrimitiveType(int.class);
		//Comparisons are always int.  (TODO: byte?)
		if (operation == Operation.CMP || operation == Operation.CMPG)
			return intType;
		//If both promotable to int, result is int.
		if (left.getType().isSubtypeOf(intType) && right.getType().isSubtypeOf(intType))
			return intType;
		//Else types must be primitive and equal.
		if (left.getType().equals(right.getType()) && left.getType() instanceof PrimitiveType)
			return (PrimitiveType)left.getType();
		throw new IllegalArgumentException("type mismatch: "+left+" "+operation+" "+right);
	}

	@Override
	public String toString() {
		return String.format("%s (%s) = %s %s, %s",
				getName(), getType(), getOperation(),
				getOperand(0).getName(), getOperand(1).getName());
	}
}

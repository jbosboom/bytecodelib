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
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import edu.mit.streamjit.util.bytecode.BasicBlock;
import edu.mit.streamjit.util.bytecode.Constant;
import edu.mit.streamjit.util.bytecode.Value;
import java.util.Iterator;

/**
 * Transfers control to one of several possible blocks by comparing a value
 * against a map of integer constants to blocks.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/15/2013
 */
public final class SwitchInst extends TerminatorInst {
	public SwitchInst(Value expr, BasicBlock defaultBlock) {
		super(defaultBlock.getType().getTypeFactory(), 2);
		setValue(expr);
		setDefault(defaultBlock);
	}

	public Value getValue() {
		return getOperand(0);
	}
	public void setValue(Value v) {
		setOperand(0, v);
	}

	public BasicBlock getDefault() {
		return (BasicBlock)getOperand(1);
	}

	public void setDefault(BasicBlock bb) {
		setOperand(1, bb);
	}

	public BasicBlock get(Constant<Integer> cst) {
		int ci = Iterables.indexOf(operands(), Predicates.<Value>equalTo(cst));
		return ci != -1 ? (BasicBlock)getOperand(ci+1) : null;
	}

	public BasicBlock put(Constant<Integer> cst, BasicBlock bb) {
		BasicBlock oldVal = get(cst);
		int ci = Iterables.indexOf(operands(), Predicates.<Value>equalTo(cst));
		if (ci != -1)
			setOperand(ci+1, bb);
		else {
			addOperand(Iterables.size(operands()), cst);
			addOperand(Iterables.size(operands()), bb);
		}
		return oldVal;
	}

	@SuppressWarnings("unchecked")
	public FluentIterable<Constant<Integer>> cases() {
		return (FluentIterable<Constant<Integer>>)(FluentIterable)operands().filter(Constant.class);
	}

	@Override
	public SwitchInst clone(Function<Value, Value> operandMap) {
		SwitchInst i = new SwitchInst(operandMap.apply(getValue()), (BasicBlock)operandMap.apply(getDefault()));
		for (Constant<Integer> c : cases())
			i.put(((Constant<?>)(operandMap.apply(c))).as(Integer.class), (BasicBlock)operandMap.apply(get(c)));
		return i;
	}

	@Override
	protected void checkOperand(int i, Value v) {
		checkArgument(v instanceof BasicBlock || v.getType().isSubtypeOf(v.getType().getTypeFactory().getType(int.class)));
		super.checkOperand(i, v);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getName());
		sb.append(": = switch ").append(getValue().getName());
		sb.append(" default ").append(getDefault().getName()).append(" ");
		for (Iterator<Value> i = operands().iterator(); i.hasNext();)
			sb.append("[").append(i.next().getName()).append(", ").append(i.next().getName()).append("], ");
		sb.delete(sb.length()-2, sb.length());
		return sb.toString();
	}
}

package edu.mit.streamjit.util.bytecode.insts;

import com.google.common.base.Function;
import static com.google.common.base.Preconditions.*;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import edu.mit.streamjit.util.bytecode.BasicBlock;
import edu.mit.streamjit.util.bytecode.Value;
import edu.mit.streamjit.util.bytecode.types.Type;
import java.util.Iterator;

/**
 * A phi instruction resolves conflicting definitions from predecessor predecessors.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/15/2013
 */
public final class PhiInst extends Instruction {
	public PhiInst(Type type) {
		super(type);
	}

	public Value get(BasicBlock b) {
		int bbi = Iterables.indexOf(operands(), Predicates.<Value>equalTo(b));
		return bbi != -1 ? getOperand(bbi+1) : null;
	}

	public Value put(BasicBlock b, Value v) {
		checkNotNull(b);
		checkNotNull(v);
		checkArgument(v.getType().isSubtypeOf(getType()), "%s not a %s", v, getType());
		int bbi = Iterables.indexOf(operands(), Predicates.<Value>equalTo(b));
		Value oldVal = get(b);
		if (bbi != -1)
			setOperand(bbi+1, v);
		else {
			addOperand(getNumOperands(), b);
			addOperand(getNumOperands(), v);
		}
		return oldVal;
	}

	public FluentIterable<BasicBlock> predecessors() {
		return operands().filter(BasicBlock.class);
	}

	public FluentIterable<Value> incomingValues() {
		return operands().filter(Predicates.not(Predicates.instanceOf(BasicBlock.class)));
	}

	@Override
	public PhiInst clone(Function<Value, Value> operandMap) {
		PhiInst i = new PhiInst(getType());
		Iterator<BasicBlock> blocks = predecessors().iterator();
		Iterator<Value> values = incomingValues().iterator();
		while (blocks.hasNext())
			i.put((BasicBlock)operandMap.apply(blocks.next()), operandMap.apply(values.next()));
		return i;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getName());
		sb.append(" (").append(getType()).append(") = phi ");
		for (Iterator<Value> i = operands().iterator(); i.hasNext();)
			sb.append("[").append(i.next().getName()).append(", ").append(i.next().getName()).append("], ");
		sb.delete(sb.length()-2, sb.length());
		return sb.toString();
	}
}

package edu.mit.streamjit.util.bytecode.insts;

import com.google.common.base.Function;
import static com.google.common.base.Preconditions.*;
import edu.mit.streamjit.util.bytecode.Value;
import edu.mit.streamjit.util.bytecode.types.ArrayType;
import edu.mit.streamjit.util.bytecode.types.NullType;

/**
 * Pushes the length of an array.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/16/2013
 */
public final class ArrayLengthInst extends Instruction {
	public ArrayLengthInst(Value array) {
		super(array.getType().getTypeFactory().getType(int.class), 1);
		setOperand(0, array);
	}

	@Override
	public ArrayLengthInst clone(Function<Value, Value> operandMap) {
		return new ArrayLengthInst(operandMap.apply(getOperand(0)));
	}

	@Override
	protected void checkOperand(int i, Value v) {
		checkArgument(v.getType() instanceof ArrayType || v.getType() instanceof NullType);
		super.checkOperand(i, v);
	}

	@Override
	public String toString() {
		return String.format("%s (%s) = arraylength %s",
				getName(), getType(), getOperand(0).getName());
	}
}

package edu.mit.streamjit.util.bytecode.insts;

import com.google.common.base.Function;
import static com.google.common.base.Preconditions.*;
import edu.mit.streamjit.util.bytecode.Value;
import edu.mit.streamjit.util.bytecode.types.ReferenceType;

/**
 * Tests if an object is of a particular type.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/15/2013
 */
public final class InstanceofInst extends Instruction {
	private final ReferenceType testType;
	public InstanceofInst(ReferenceType testType) {
		super(testType.getTypeFactory().getPrimitiveType(boolean.class), 1);
		this.testType = testType;
	}
	public InstanceofInst(ReferenceType testType, Value v) {
		this(testType);
		setOperand(0, v);
	}

	public ReferenceType getTestType() {
		return testType;
	}

	@Override
	public InstanceofInst clone(Function<Value, Value> operandMap) {
		return new InstanceofInst(getTestType(), operandMap.apply(getOperand(0)));
	}

	@Override
	protected void checkOperand(int i, Value v) {
		checkElementIndex(i, 1);
		checkArgument(v.getType().isSubtypeOf(v.getType().getTypeFactory().getType(Object.class)));
		super.checkOperand(i, v);
	}

	@Override
	public String toString() {
		return String.format("%s (%s) = %s instanceof %s",
				getName(), getType(), getOperand(0).getName(), testType);
	}
}

package edu.mit.streamjit.util.bytecode.insts;

import com.google.common.base.Function;
import static com.google.common.base.Preconditions.*;
import edu.mit.streamjit.util.bytecode.Value;
import edu.mit.streamjit.util.bytecode.types.ReturnType;
import edu.mit.streamjit.util.bytecode.types.VoidType;

/**
 * Returns to the caller's stack frame, possibly with a value.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/11/2013
 */
public final class ReturnInst extends TerminatorInst {
	private final ReturnType returnType;
	public ReturnInst(ReturnType returnType) {
		super(returnType.getTypeFactory(), returnType instanceof VoidType ? 0 : 1);
		this.returnType = returnType;
	}
	public ReturnInst(ReturnType returnType, Value returnValue) {
		super(returnType.getTypeFactory(), returnType instanceof VoidType ? 0 : 1);
		this.returnType = returnType;
		if (returnType instanceof VoidType)
			checkArgument(returnValue == null, "returning a value with void ReturnInst: %s", returnValue);
		setOperand(0, returnValue);
	}

	public ReturnType getReturnType() {
		return returnType;
	}

	@Override
	public ReturnInst clone(Function<Value, Value> operandMap) {
		if (getNumOperands() == 1)
			return new ReturnInst(getReturnType(), operandMap.apply(getOperand(0)));
		return new ReturnInst(getReturnType());
	}

	@Override
	protected void checkOperand(int i, Value v) {
		if (getReturnType() instanceof VoidType)
			throw new IllegalStateException("void returns don't take operands");
		checkArgument(i == 0, "ReturnInsts take only one operand: %s %s", i, v);
		checkArgument(v.getType().isSubtypeOf(returnType), "not subtype of %s: %s", returnType, v);
		super.checkOperand(i, v);
	}

	@Override
	public String toString() {
		return String.format("%s: return %s", getName(),
				getReturnType() instanceof VoidType ? "void" : getOperand(0).getName());
	}
}

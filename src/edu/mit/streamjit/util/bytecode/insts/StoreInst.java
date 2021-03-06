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
import edu.mit.streamjit.util.bytecode.Field;
import edu.mit.streamjit.util.bytecode.LocalVariable;
import edu.mit.streamjit.util.bytecode.Value;
import edu.mit.streamjit.util.bytecode.types.FieldType;
import edu.mit.streamjit.util.bytecode.types.InstanceFieldType;
import edu.mit.streamjit.util.bytecode.types.ReferenceType;
import edu.mit.streamjit.util.bytecode.types.RegularType;

/**
 * Stores a static or instance field.  (Does not store array elements.)
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/14/2013
 */
public final class StoreInst extends Instruction {
	public StoreInst(Field f) {
		super(f.getType().getTypeFactory().getVoidType(), f.isStatic() ? 2 : 3);
		setOperand(0, f);
	}
	public StoreInst(Field f, Value data) {
		this(f);
		setOperand(1, data);
	}
	public StoreInst(Field f, Value data, Value instance) {
		this(f);
		checkArgument(!f.isStatic(), "storing into static field %s with instance %s", f, instance);
		setOperand(1, data);
		setOperand(2, instance);
	}
	public StoreInst(LocalVariable v, Value data) {
		super(v.getType().getTypeFactory().getVoidType(), 2);
		setOperand(0, v);
		setOperand(1, data);
	}

	public Value getLocation() {
		return getOperand(0);
	}
	public void setLocation(Value f) {
		setOperand(0, f);
	}
	public Value getData() {
		return getOperand(1);
	}
	public void setData(Value v) {
		setOperand(1, v);
	}
	public Value getInstance() {
		return getOperand(2);
	}
	public void setInstance(Value v) {
		setOperand(2, v);
	}

	@Override
	public StoreInst clone(Function<Value, Value> operandMap) {
		Value newLocation = operandMap.apply(getLocation());
		if (getNumOperands() == 2)
			if (newLocation instanceof LocalVariable)
				return new StoreInst((LocalVariable)newLocation, operandMap.apply(getData()));
			else
				return new StoreInst((Field)newLocation, operandMap.apply(getData()));
		return new StoreInst((Field)newLocation, operandMap.apply(getData()), operandMap.apply(getInstance()));
	}

	@Override
	protected void checkOperand(int i, Value v) {
		checkElementIndex(i, 3);
		if (i == 0) {
			checkArgument(v.getType() instanceof FieldType);
		} else if (i == 1) {
			RegularType type = ((FieldType)getLocation().getType()).getFieldType();
			checkArgument(v.getType().isSubtypeOf(type), "%s (%s) not compatible with %s", v, v.getType(), type);
		} else if (i == 2) {
			Field f = (Field)getLocation();
			if (f != null) {
				checkState(f.getType() instanceof InstanceFieldType);
				ReferenceType instanceType = ((InstanceFieldType)f.getType()).getInstanceType();
				checkArgument(v.getType().isSubtypeOf(instanceType), "%s (%s) not an instance of field's instance type %s", v, v.getType(), instanceType);
			}
		}
		super.checkOperand(i, v);
	}

	@Override
	public String toString() {
		if (getLocation() instanceof LocalVariable)
			return String.format("%s: store %s = %s",
						getName(),
						getLocation().getName(),
						getData().getName());
		else {
			Field f = (Field)getLocation();
			if (f.isStatic())
				return String.format("%s: putstatic %s#%s = %s",
						getName(),
						f.getParent().getName(), f.getName(),
						getData().getName());
			else
				return String.format("%s: putfield %s#%s of %s = %s",
						getName(),
						f.getParent().getName(), f.getName(),
						getInstance().getName(),
						getData().getName());
		}
	}
}

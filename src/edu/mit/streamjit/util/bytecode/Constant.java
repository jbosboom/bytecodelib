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
package edu.mit.streamjit.util.bytecode;

import static com.google.common.base.Preconditions.*;
import com.google.common.primitives.Primitives;
import edu.mit.streamjit.util.bytecode.types.NullType;
import edu.mit.streamjit.util.bytecode.types.PrimitiveType;
import edu.mit.streamjit.util.bytecode.types.RegularType;
import edu.mit.streamjit.util.bytecode.types.Type;

/**
 * A constant Value that can be loaded from the constant pool or pushed onto the
 * operand stack as an immediate operand.
 *
 * Constants are immutable and interned by ConstantFactory.  Note that this
 * implies replaceAllUsesWith() is not supported by Constant as it would replace
 * uses in the whole program.
 *
 * The constant types are the primitive types (represented by their wrappers),
 * String, Class<?>, and the null type (represented as a Constant<?> with a null
 * value).
 * @param <T> the type of the constant value (not to be confused with the IR
 * type, which may be primitive)
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/12/2013
 */
public final class Constant<T> extends Value implements Parented<Module> {
	private final Module parent;
	/**
	 * The constant value.  An instance of Boolean, Byte, Char, Short, Integer,
	 * Long, Float, Double, String or Class, or null.
	 */
	private final T constant;

	Constant(Type type, T constant, Module parent) {
		super(type);
		this.parent = checkNotNull(parent);
		if (type instanceof NullType)
			checkArgument(constant == null, "NullType but not null value");
		else if (type instanceof PrimitiveType)
			checkArgument(Primitives.wrap(((RegularType)type).getKlass().getBackingClass()).equals(constant.getClass()),
					"got a %s (%s), but type is %s", constant, constant.getClass().getName(), type);
		else if (type.equals(parent.types().getType(parent.getKlass(String.class))))
			checkArgument(constant instanceof String,
					"got a %s (%s), but type is %s", constant, constant.getClass().getName(), type);
		else if (type.equals(parent.types().getType(parent.getKlass(Class.class))))
			checkArgument(constant instanceof Class,
					"got a %s (%s), but type is %s", constant, constant.getClass().getName(), type);
		else
			checkArgument(false,
					"bad constant type %s; value was %s (%s)", type, constant, constant.getClass().getName());
		this.constant = constant;
		setName(toString());
	}

	@Override
	public Module getParent() {
		return parent;
	}

	public T getConstant() {
		return constant;
	}

	/**
	 * Casts this Constant to the given Constant<X> if this constant really is
	 * of that type.
	 * @param <X> the constant type to cast to
	 * @param klass the constant type to cast to
	 * @return this constant, cast to the given type
	 */
	@SuppressWarnings("unchecked")
	public <X> Constant<X> as(Class<X> klass) {
		checkArgument(klass.isInstance(getConstant()));
		return (Constant<X>)this;
	}

	/**
	 * Throws UnsupportedOperationException.
	 * @param value will be ignored
	 * @throws UnsupportedOperationException always
	 * @deprecated Constants are interned, so RAUW rewrites the whole program,
	 * which is seldom desired.
	 */
	@Deprecated
	@Override
	public void replaceAllUsesWith(Value value) {
		throw new UnsupportedOperationException("constants don't support RAUW");
	}

	@Override
	public String toString() {
		if (constant == null)
			return "null";
		if (constant instanceof String)
			//TODO: escape and quote this string
			return (String)constant;
		if (constant instanceof Class)
			return constant.toString() + ".class";
		return "("+getType().toString()+")"+constant;
	}
}

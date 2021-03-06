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
package edu.mit.streamjit.util.bytecode.types;

import static com.google.common.base.Preconditions.checkNotNull;
import edu.mit.streamjit.util.bytecode.Klass;
import edu.mit.streamjit.util.bytecode.Module;

/**
 * A ReturnType is a type that can be used as the return type of a method; that
 * is, a RegularType or void.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/7/2013
 */
public abstract class ReturnType extends Type {
	private final Klass klass;
	ReturnType(Klass klass) {
		this.klass = checkNotNull(klass);
	}

	public Klass getKlass() {
		return klass;
	}

	/**
	 * Returns this type's module.  All ReturnTypes implicitly belong to a Module (the Module their Klass
	 * belongs to).
	 * @return the Module this ReturnType belongs to
	 */
	@Override
	public Module getModule() {
		return getKlass().getParent();
	}

	/**
	 * Returns this type's TypeFactory.  All ReturnTypes implicitly have a
	 * TypeFactory (the TypeFactory of the Module they belong to)
	 * @return the TypeFactory of the Module this ReturnType belongs to
	 */
	@Override
	public TypeFactory getTypeFactory() {
		return getModule().types();
	}

	public abstract String getDescriptor();

	@Override
	public String toString() {
		return klass.getName();
	}
}

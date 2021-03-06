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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.mit.streamjit.util.bytecode.insts.Instruction;
import edu.mit.streamjit.util.bytecode.insts.TerminatorInst;
import edu.mit.streamjit.util.bytecode.types.BasicBlockType;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/6/2013
 */
public class BasicBlock extends Value implements Parented<Method> {
	@IntrusiveList.Previous
	private BasicBlock previous;
	@IntrusiveList.Next
	private BasicBlock next;
	@ParentedList.Parent
	private Method parent;
	private final IntrusiveList<Instruction> instructions = new ParentedList<>(this, Instruction.class);
	/**
	 * Creates a new, empty BasicBlock not attached to any parent.  The Module
	 * is used to get the correct BasicBlockType.
	 * @param module the module this BasicBlock is associated with
	 */
	public BasicBlock(Module module) {
		super(module.types().getBasicBlockType());
	}

	public BasicBlock(Module module, String name) {
		this(module);
		setName(name);
	}

	/**
	 * Creates a new, empty BasicBlock and appends it to the given method.
	 * @param method the method to append to
	 */
	public BasicBlock(Method method) {
		this(method.getParent().getParent());
		method.basicBlocks().add(this);
	}

	public BasicBlock(Method method, String name) {
		this(method.getParent().getParent(), name);
		method.basicBlocks().add(this);
	}

	@Override
	public BasicBlockType getType() {
		return (BasicBlockType)super.getType();
	}

	@Override
	public Method getParent() {
		return parent;
	}

	public List<Instruction> instructions() {
		//TODO: figure out how to make this immutable when the parent is
		//immutable.  Note that we add to this list during resolution.
		return instructions;
	}

	public TerminatorInst getTerminator() {
		if (instructions.isEmpty())
			return null;
		Instruction lastInst = instructions.listIterator(instructions.size()).previous();
		return lastInst instanceof TerminatorInst ? (TerminatorInst)lastInst : null;
	}

	public Iterable<BasicBlock> predecessors() {
		return new Iterable<BasicBlock>() {
			@Override
			public Iterator<BasicBlock> iterator() {
				ImmutableSet.Builder<BasicBlock> builder = ImmutableSet.builder();
				for (User user : users().elementSet())
					if (user instanceof TerminatorInst && ((Instruction)user).getParent() != null)
						builder.add(((Instruction)user).getParent());
				return builder.build().iterator();
			}
		};
	}

	public Iterable<BasicBlock> successors() {
		TerminatorInst terminator = getTerminator();
		return terminator != null ? terminator.successors() : Collections.<BasicBlock>emptyList();
	}

	public BasicBlock removeFromParent() {
		checkState(getParent() != null);
		getParent().basicBlocks().remove(this);
		return this;
	}

	public void eraseFromParent() {
		removeFromParent();
		for (Instruction i : ImmutableList.copyOf(instructions()))
			i.eraseFromParent();
	}
}

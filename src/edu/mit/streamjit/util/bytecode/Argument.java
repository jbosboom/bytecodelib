package edu.mit.streamjit.util.bytecode;

import edu.mit.streamjit.util.bytecode.types.RegularType;

/**
 * An Argument represents an argument to a Method.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/6/2013
 */
public class Argument extends Value implements Parented<Method> {
	private final Method parent;
	public Argument(Method parent, RegularType type) {
		super(type);
		this.parent = parent;
	}
	public Argument(Method parent, RegularType type, String name) {
		super(type, name);
		this.parent = parent;
	}

	public boolean isReceiver() {
		return parent.hasReceiver() && parent.arguments().indexOf(this) == 0;
	}

	@Override
	public Method getParent() {
		return parent;
	}

	@Override
	public RegularType getType() {
		return (RegularType)super.getType();
	}

	@Override
	public String toString() {
		if (getName() != null)
			return getName();
		return getClass().getSimpleName()+"@"+hashCode();
	}
}

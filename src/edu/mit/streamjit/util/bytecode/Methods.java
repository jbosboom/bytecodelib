package edu.mit.streamjit.util.bytecode;

import edu.mit.streamjit.util.bytecode.insts.CallInst;
import edu.mit.streamjit.util.bytecode.insts.ReturnInst;
import java.util.EnumSet;

/**
 * Contains static methods for common Method operations.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 10/3/2014
 */
public final class Methods {
	private Methods() {}

	/**
	 * Creates a default (no-arg) constructor for the given class that simply
	 * calls the superclass default constructor and returns.
	 * @param klass the class to create a default constructor for
	 * @return the newly-created default constructor
	 */
	public static Method createDefaultConstructor(Klass klass) {
		Module module = klass.getParent();
		Method init = new Method("<init>",
				module.types().getMethodType(module.types().getType(klass)),
				EnumSet.of(Modifier.PUBLIC),
				klass);
		BasicBlock initBlock = new BasicBlock(init);
		Method objCtor = klass.getSuperclass().getMethods("<init>").iterator().next();
		initBlock.instructions().add(new CallInst(objCtor));
		initBlock.instructions().add(new ReturnInst(module.types().getVoidType()));
		return init;
	}
}

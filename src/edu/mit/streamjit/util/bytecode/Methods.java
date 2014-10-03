package edu.mit.streamjit.util.bytecode;

import static com.google.common.base.Preconditions.checkArgument;
import edu.mit.streamjit.util.bytecode.insts.CallInst;
import edu.mit.streamjit.util.bytecode.insts.LoadInst;
import edu.mit.streamjit.util.bytecode.insts.ReturnInst;
import edu.mit.streamjit.util.bytecode.types.MethodType;
import edu.mit.streamjit.util.bytecode.types.VoidType;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

	/**
	 * Creates a method that loads a MethodHandle from a static field and
	 * invokes it on the method arguments (except the receiver, if the method is
	 * nonstatic).
	 *
	 * TODO: if the field and method are both nonstatic, load the field from the
	 * receiver?  (would be similar to MethodHandleProxies, though it wouldn't
	 * understand bridge methods, so...)
	 * @param klass the class to create the method in
	 * @param field the field to load the method handle from
	 * @param modifiers the new method's modifiers
	 * @param name the new method's name
	 * @param mhType the type of the method handle being invoked
	 * @return the newly-created method
	 */
	public static Method invokeExactFromField(Klass klass, Field field, Set<Modifier> modifiers, String name, java.lang.invoke.MethodType mhType) {
		checkArgument(field.getType().getFieldType().getKlass().getBackingClass() == MethodHandle.class,
				"not a MethodHandle-type field: %s", field);
		Module module = klass.getParent();
		MethodType type = module.types().getMethodType(mhType);
		if (!modifiers.contains(Modifier.STATIC))
			type = type.prependArgument(module.types().getRegularType(klass));
		Method n = new Method(name, type, modifiers, klass);
		BasicBlock block = new BasicBlock(n);
		LoadInst getHandle = new LoadInst(field);

		List<Value> invokeArgs = new ArrayList<>(n.arguments());
		MethodType callDescriptor = n.getType();
		if (n.hasReceiver()) {
			invokeArgs.remove(0);
			callDescriptor = callDescriptor.dropFirstArgument();
		}
		invokeArgs.add(0, getHandle);
		callDescriptor = callDescriptor.prependArgument(module.types().getRegularType(MethodHandle.class));

		Method invokeExact = module.getKlass(MethodHandle.class).getMethod("invokeExact", module.types().getMethodType(Object.class, MethodHandle.class, Object[].class));
		CallInst invoke = new CallInst(invokeExact, callDescriptor, invokeArgs.toArray(new Value[0]));
		//TODO: allow passing void value to return (just ignore it)
		ReturnInst ret = invoke.getType() instanceof VoidType ?
				new ReturnInst(invoke.getType()) :
				new ReturnInst(invoke.getType(), invoke);
		block.instructions().addAll(Arrays.asList(getHandle, invoke, ret));
		return n;
	}
}

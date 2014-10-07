package edu.mit.streamjit.util.bytecode.methodhandles;

import static com.google.common.base.Preconditions.checkArgument;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 10/6/2014 (some methods from StreamJIT @since 10/3/2013)
 */
public final class Combinators {
	private Combinators() {throw new AssertionError();}

	private static final MethodHandle VOID_VOID_NOP = MethodHandles.identity(Object.class)
			.bindTo(null).asType(MethodType.methodType(void.class));
	/**
	 * Returns a method handle that takes the given argument types, returns
	 * void, and does nothing when called.
	 * @param arguments the argument types
	 * @return a no-op method handle
	 */
	public static MethodHandle nop(Class<?>... arguments) {
		return MethodHandles.dropArguments(VOID_VOID_NOP, 0, arguments);
	}

	/**
	 * Returns a method handle that calls the given method handles in sequence,
	 * ignoring their return values.  The given handles must all take the same
	 * parameters.  They may have any return type, but any returned values will
	 * be ignored.  If no handles are given, the returned handle does nothing.
	 * @param handles the handles to invoke
	 * @return a method handle approximating semicolons
	 */
	public static MethodHandle semicolon(MethodHandle... handles) {
		if (handles.length == 0)
			return nop();
		MethodType type = handles[0].type().changeReturnType(void.class);
		if (handles.length == 1)
			return handles[0].asType(type);
		MethodHandle chain = nop(type.parameterArray());
		for (int i = handles.length-1; i >= 0; --i) {
			checkArgument(handles[i].type().parameterList().equals(type.parameterList()), "Type mismatch in "+Arrays.toString(handles));
			chain = MethodHandles.foldArguments(chain, handles[i].asType(type));
		}
		return chain;
	}

	/**
	 * Returns a method handle that calls the given method handles in sequence,
	 * ignoring their return values.  The given handles must all take the same
	 * parameters.  They may have any return type, but any returned values will
	 * be ignored.  If no handles are given, the returned handle does nothing.
	 * @param handles the handles to invoke
	 * @return a method handle approximating semicolons
	 */
	public static MethodHandle semicolon(List<MethodHandle> handles) {
		return semicolon(handles.toArray(new MethodHandle[0]));
	}

	/**
	 * Returns a method handle that invokes the target handle with the result of
	 * invoking the given argument handles.
	 * @param target the method handle to invoke
	 * @param args the method handles returning arguments for the target
	 * @return a method handle that invokes the target with the result of
	 * invoking the given argument handles
	 */
	public static MethodHandle apply(MethodHandle target, MethodHandle... args) {
		for (MethodHandle a : args)
			target = MethodHandles.collectArguments(target, 0,
					a.asType(a.type().changeReturnType(target.type().parameterType(0))));
		return target;
	}

	/**
	 * Returns a method handle that invokes the target handle with the result of
	 * invoking the given argument handles.
	 * @param target the method handle to invoke
	 * @param args the method handles returning arguments for the target
	 * @return a method handle that invokes the target with the result of
	 * invoking the given argument handles
	 */
	public static MethodHandle apply(MethodHandle target, Iterable<MethodHandle> args) {
		for (MethodHandle a : args)
			target = MethodHandles.collectArguments(target, 0,
					a.asType(a.type().changeReturnType(target.type().parameterType(0))));
		return target;
	}

	/**
	 * Returns a method handle that invokes the target handle with the result of
	 * invoking the given argument handles.
	 * @param target the method handle to invoke
	 * @param args the method handles returning arguments for the target
	 * @return a method handle that invokes the target with the result of
	 * invoking the given argument handles
	 */
	public static MethodHandle apply(MethodHandle target, Iterator<MethodHandle> args) {
		while (args.hasNext()) {
			MethodHandle a = args.next();
			target = MethodHandles.collectArguments(target, 0,
					a.asType(a.type().changeReturnType(target.type().parameterType(0))));
		}
		return target;
	}
}

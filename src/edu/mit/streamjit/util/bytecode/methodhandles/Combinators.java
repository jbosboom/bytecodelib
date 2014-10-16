package edu.mit.streamjit.util.bytecode.methodhandles;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import static edu.mit.streamjit.util.bytecode.methodhandles.LookupUtils.findStatic;
import static edu.mit.streamjit.util.bytecode.methodhandles.LookupUtils.param;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
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
	private static final Lookup LOOKUP = MethodHandles.lookup();
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

	private static final MethodHandle METHODHANDLE_ARRAY_GETTER = MethodHandles.arrayElementGetter(MethodHandle[].class);
	/**
	 * Returns a MethodHandle with a leading int argument that selects one of
	 * the MethodHandles in the given array, which is invoked with the
	 * remaining arguments.
	 * @param cases the cases to select from
	 * @return a MethodHandle approximating the switch statement
	 */
	public static MethodHandle tableswitch(MethodHandle[] cases) {
		checkArgument(cases.length >= 1);
		MethodType type = cases[0].type();
		for (MethodHandle mh : cases)
			checkArgument(mh.type().equals(type), "Type mismatch in "+Arrays.toString(cases));
		MethodHandle selector = METHODHANDLE_ARRAY_GETTER.bindTo(cases);
		//Replace the index with the handle to invoke, passing it to an invoker.
		return MethodHandles.filterArguments(MethodHandles.exactInvoker(type), 0, selector);
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

	private static int _arraylength(boolean[] a) {
		return a.length;
	}
	private static int _arraylength(byte[] a) {
		return a.length;
	}
	private static int _arraylength(short[] a) {
		return a.length;
	}
	private static int _arraylength(char[] a) {
		return a.length;
	}
	private static int _arraylength(int[] a) {
		return a.length;
	}
	private static int _arraylength(long[] a) {
		return a.length;
	}
	private static int _arraylength(float[] a) {
		return a.length;
	}
	private static int _arraylength(double[] a) {
		return a.length;
	}
	private static int _arraylength(Object[] a) {
		return a.length;
	}
	private static final MethodHandle REFERENCE_ARRAYLENGTH = findStatic(LOOKUP, "_arraylength", param(0, Object[].class));
	private static final ImmutableMap<Class<?>, MethodHandle> PRIMITIVE_ARRAYLENGTH;
	static {
		ImmutableMap.Builder<Class<?>, MethodHandle> builder = ImmutableMap.builder();
		for (Class<?> c : Primitives.allPrimitiveTypes())
			if (c != void.class) {
				Class<?> arrayClass = Array.newInstance(c, 0).getClass();
				builder.put(arrayClass, findStatic(LOOKUP, Combinators.class, "_arraylength", param(0, arrayClass)));
			}
		PRIMITIVE_ARRAYLENGTH = builder.build();
	}
	/**
	 * Returns a method handle taking one argument of the given array type and
	 * returning its length (as an int).
	 * @param arrayClass an array type
	 * @return a method handle returning the length of arrays of the given type
	 */
	public static MethodHandle arraylength(Class<?> arrayClass) {
		checkArgument(arrayClass.isArray(), "%s not an array class", arrayClass);
		if (arrayClass.getComponentType().isPrimitive()) {
			MethodHandle handle = PRIMITIVE_ARRAYLENGTH.get(arrayClass);
			assert handle != null : arrayClass;
			return handle;
		}
		return REFERENCE_ARRAYLENGTH.asType(MethodType.methodType(int.class, arrayClass));
	}

	private static final MethodHandle INTEGER_SUM = findStatic(Integer.class, "sum");
	/**
	 * Returns an int -> int method handle that returns the sum of its argument
	 * and the given addend.
	 *
	 * TODO: it's not clear what the proper interface for math combinators is,
	 * or if they're generally useful.  StreamJIT only needs this one (and sub
	 * which justs uses negative addends).
	 * @param addend the addend to add
	 * @return a method handle that adds the given addend
	 */
	public static MethodHandle adder(int addend) {
		return MethodHandles.insertArguments(INTEGER_SUM, 1, addend);
	}
}

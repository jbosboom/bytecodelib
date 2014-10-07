package edu.mit.streamjit.util.bytecode.methodhandles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Convenience methods for {@link MethodHandles.Lookup} methods that infer
 * arguments when unambiguous and do not throw checked exceptions.
 *
 * Methods taking a Lookup and not a container class assume the container class
 * is the lookup class.  Methods taking a container class and not a Lookup use
 * the public lookup.
 *
 * Methods not taking a type look reflectively at the class for a member with
 * the given name.  If there is exactly one matching member, it will be looked
 * up as though no reflection was performed (i.e., not using Lookup.unreflect*).
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 10/6/2014 (based on StreamJIT LookupUtils, @since 11/14/2013)
 */
public final class LookupUtils {
	private LookupUtils() {throw new AssertionError();}

	public static MethodHandle findVirtual(Lookup lookup, Class<?> container, String name, MethodType type) {
		try {
			return lookup.findVirtual(container, name, type);
		} catch (NoSuchMethodException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findVirtual(Lookup lookup, String name, MethodType type) {
		return findVirtual(lookup, lookup.lookupClass(), name, type);
	}

	public static MethodHandle findVirtual(Class<?> container, String name, MethodType type) {
		return findVirtual(MethodHandles.publicLookup(), container, name, type);
	}

	public static MethodHandle findVirtual(Lookup lookup, Class<?> container, String name, Predicate<MethodType> typeFilter) {
		List<Method> matching = Arrays.stream(container.getDeclaredMethods())
				.filter(m -> !Modifier.isStatic(m.getModifiers()))
				.filter(m -> m.getName().equals(name))
				.filter(m -> typeFilter.test(MethodType.methodType(m.getReturnType(), m.getParameterTypes())))
				.collect(Collectors.toList());
		if (matching.size() != 1)
			throw new RuntimeException(String.format("%s %s %s %s: %s", lookup, container, name, typeFilter, matching));
		Method m = matching.get(0);
		return findVirtual(lookup, container, name, MethodType.methodType(m.getReturnType(), m.getParameterTypes()));
	}

	public static MethodHandle findVirtual(Lookup lookup, String name, Predicate<MethodType> typeFilter) {
		return findVirtual(lookup, lookup.lookupClass(), name, typeFilter);
	}

	public static MethodHandle findVirtual(Class<?> container, String name, Predicate<MethodType> typeFilter) {
		return findVirtual(MethodHandles.publicLookup(), container, name, typeFilter);
	}

	public static MethodHandle findVirtual(Lookup lookup, Class<?> container, String name) {
		return findVirtual(lookup, container, name, x -> true);
	}

	public static MethodHandle findVirtual(Lookup lookup, String name) {
		return findVirtual(lookup, lookup.lookupClass(), name);
	}

	public static MethodHandle findVirtual(Class<?> container, String name) {
		return findVirtual(MethodHandles.publicLookup(), container, name);
	}

	//TODO: findSpecial

	//TODO: findConstructor

	public static MethodHandle findStatic(Lookup lookup, Class<?> container, String name, MethodType type) {
		try {
			return lookup.findStatic(container, name, type);
		} catch (NoSuchMethodException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findStatic(Lookup lookup, String name, MethodType type) {
		return findStatic(lookup, lookup.lookupClass(), name, type);
	}

	public static MethodHandle findStatic(Class<?> container, String name, MethodType type) {
		return findStatic(MethodHandles.publicLookup(), container, name, type);
	}

	public static MethodHandle findStatic(Lookup lookup, Class<?> container, String name, Predicate<MethodType> typeFilter) {
		List<Method> matching = Arrays.stream(container.getDeclaredMethods())
				.filter(m -> Modifier.isStatic(m.getModifiers()))
				.filter(m -> m.getName().equals(name))
				.filter(m -> typeFilter.test(MethodType.methodType(m.getReturnType(), m.getParameterTypes())))
				.collect(Collectors.toList());
		if (matching.size() != 1)
			throw new RuntimeException(String.format("%s %s %s %s: %s", lookup, container, name, typeFilter, matching));
		Method m = matching.get(0);
		return findStatic(lookup, container, name, MethodType.methodType(m.getReturnType(), m.getParameterTypes()));
	}

	public static MethodHandle findStatic(Lookup lookup, String name, Predicate<MethodType> typeFilter) {
		return findStatic(lookup, lookup.lookupClass(), name, typeFilter);
	}

	public static MethodHandle findStatic(Class<?> container, String name, Predicate<MethodType> typeFilter) {
		return findStatic(MethodHandles.publicLookup(), container, name, typeFilter);
	}

	public static MethodHandle findStatic(Lookup lookup, Class<?> container, String name) {
		return findStatic(lookup, container, name, x -> true);
	}

	public static MethodHandle findStatic(Lookup lookup, String name) {
		return findStatic(lookup, lookup.lookupClass(), name);
	}

	public static MethodHandle findStatic(Class<?> container, String name) {
		return findStatic(MethodHandles.publicLookup(), container, name);
	}



	public static Predicate<MethodType> params(int parameterCount) {
		return t -> t.parameterCount() == parameterCount;
	}

	public static Predicate<MethodType> noPrimParam() {
		return t -> !t.parameterList().stream().anyMatch(Class::isPrimitive);
	}

	public static Predicate<MethodType> noRefParam() {
		return t -> t.parameterList().stream().allMatch(Class::isPrimitive);
	}
}

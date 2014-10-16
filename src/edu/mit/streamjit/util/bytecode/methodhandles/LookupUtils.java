package edu.mit.streamjit.util.bytecode.methodhandles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
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

	//<editor-fold defaultstate="collapsed" desc="findVirtual">
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
	//</editor-fold>

	//TODO: findSpecial

	//<editor-fold defaultstate="collapsed" desc="findConstructor">
	public static MethodHandle findConstructor(Lookup lookup, Class<?> container, MethodType type) {
		try {
			return lookup.findConstructor(container, type);
		} catch (NoSuchMethodException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findConstructor(Lookup lookup, MethodType type) {
		return findConstructor(lookup, lookup.lookupClass(), type);
	}

	public static MethodHandle findConstructor(Class<?> container, MethodType type) {
		return findConstructor(MethodHandles.publicLookup(), container, type);
	}

	public static MethodHandle findConstructor(Lookup lookup, Class<?> container, Predicate<MethodType> typeFilter) {
		List<Constructor<?>> matching = Arrays.stream(container.getDeclaredConstructors())
				.filter(m -> typeFilter.test(MethodType.methodType(void.class, m.getParameterTypes())))
				.collect(Collectors.toList());
		if (matching.size() != 1)
			throw new RuntimeException(String.format("%s %s %s: %s", lookup, container, typeFilter, matching));
		Constructor<?> m = matching.get(0);
		return findConstructor(lookup, container, MethodType.methodType(void.class, m.getParameterTypes()));
	}

	public static MethodHandle findConstructor(Lookup lookup, Predicate<MethodType> typeFilter) {
		return findConstructor(lookup, lookup.lookupClass(), typeFilter);
	}

	public static MethodHandle findConstructor(Class<?> container, Predicate<MethodType> typeFilter) {
		return findConstructor(MethodHandles.publicLookup(), container, typeFilter);
	}

	public static MethodHandle findConstructor(Lookup lookup, Class<?> container) {
		return findConstructor(lookup, container, x -> true);
	}

	public static MethodHandle findConstructor(Lookup lookup) {
		return findConstructor(lookup, lookup.lookupClass());
	}

	public static MethodHandle findConstructor(Class<?> container) {
		return findConstructor(MethodHandles.publicLookup(), container);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="findStatic">
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
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="findGetter">
	public static MethodHandle findGetter(Lookup lookup, Class<?> container, String name, Class<?> type) {
		try {
			return lookup.findGetter(container, name, type);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findGetter(Class<?> container, String name, Class<?> type) {
		return findGetter(MethodHandles.publicLookup(), container, name, type);
	}

	public static MethodHandle findGetter(Lookup lookup, String name, Class<?> type) {
		return findGetter(lookup, lookup.lookupClass(), name, type);
	}

	public static MethodHandle findGetter(Lookup lookup, Class<?> container, String name) {
		try {
			return findGetter(lookup, container, name, container.getDeclaredField(name).getType());
		} catch (NoSuchFieldException | SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findGetter(Class<?> container, String name) {
		return findGetter(MethodHandles.publicLookup(), container, name);
	}

	public static MethodHandle findGetter(Lookup lookup, String name) {
		return findGetter(lookup, lookup.lookupClass(), name);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="findSetter">
	public static MethodHandle findSetter(Lookup lookup, Class<?> container, String name, Class<?> type) {
		try {
			return lookup.findSetter(container, name, type);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findSetter(Class<?> container, String name, Class<?> type) {
		return findSetter(MethodHandles.publicLookup(), container, name, type);
	}

	public static MethodHandle findSetter(Lookup lookup, String name, Class<?> type) {
		return findSetter(lookup, lookup.lookupClass(), name, type);
	}

	public static MethodHandle findSetter(Lookup lookup, Class<?> container, String name) {
		try {
			return findSetter(lookup, container, name, container.getDeclaredField(name).getType());
		} catch (NoSuchFieldException | SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findSetter(Class<?> container, String name) {
		return findSetter(MethodHandles.publicLookup(), container, name);
	}

	public static MethodHandle findSetter(Lookup lookup, String name) {
		return findSetter(lookup, lookup.lookupClass(), name);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="findStaticGetter">
	public static MethodHandle findStaticGetter(Lookup lookup, Class<?> container, String name, Class<?> type) {
		try {
			return lookup.findStaticGetter(container, name, type);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findStaticGetter(Class<?> container, String name, Class<?> type) {
		return findStaticGetter(MethodHandles.publicLookup(), container, name, type);
	}

	public static MethodHandle findStaticGetter(Lookup lookup, String name, Class<?> type) {
		return findStaticGetter(lookup, lookup.lookupClass(), name, type);
	}

	public static MethodHandle findStaticGetter(Lookup lookup, Class<?> container, String name) {
		try {
			return findStaticGetter(lookup, container, name, container.getDeclaredField(name).getType());
		} catch (NoSuchFieldException | SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findStaticGetter(Class<?> container, String name) {
		return findStaticGetter(MethodHandles.publicLookup(), container, name);
	}

	public static MethodHandle findStaticGetter(Lookup lookup, String name) {
		return findStaticGetter(lookup, lookup.lookupClass(), name);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="findStaticSetter">
	public static MethodHandle findStaticSetter(Lookup lookup, Class<?> container, String name, Class<?> type) {
		try {
			return lookup.findStaticSetter(container, name, type);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findStaticSetter(Class<?> container, String name, Class<?> type) {
		return findStaticSetter(MethodHandles.publicLookup(), container, name, type);
	}

	public static MethodHandle findStaticSetter(Lookup lookup, String name, Class<?> type) {
		return findStaticSetter(lookup, lookup.lookupClass(), name, type);
	}

	public static MethodHandle findStaticSetter(Lookup lookup, Class<?> container, String name) {
		try {
			return findStaticSetter(lookup, container, name, container.getDeclaredField(name).getType());
		} catch (NoSuchFieldException | SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MethodHandle findStaticSetter(Class<?> container, String name) {
		return findStaticSetter(MethodHandles.publicLookup(), container, name);
	}

	public static MethodHandle findStaticSetter(Lookup lookup, String name) {
		return findStaticSetter(lookup, lookup.lookupClass(), name);
	}
	//</editor-fold>


	//<editor-fold defaultstate="collapsed" desc="MethodType predicates">
	public static Predicate<MethodType> params(int parameterCount) {
		return t -> t.parameterCount() == parameterCount;
	}

	public static Predicate<MethodType> noPrimParam() {
		return t -> !t.parameterList().stream().anyMatch(Class::isPrimitive);
	}

	public static Predicate<MethodType> noRefParam() {
		return t -> t.parameterList().stream().allMatch(Class::isPrimitive);
	}
	//</editor-fold>
}

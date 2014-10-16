package edu.mit.streamjit.util.bytecode.methodhandles;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.Lists;
import edu.mit.streamjit.util.bytecode.Access;
import edu.mit.streamjit.util.bytecode.Klass;
import edu.mit.streamjit.util.bytecode.Methods;
import edu.mit.streamjit.util.bytecode.Modifier;
import edu.mit.streamjit.util.bytecode.Module;
import edu.mit.streamjit.util.bytecode.ModuleClassLoader;
import static edu.mit.streamjit.util.bytecode.methodhandles.LookupUtils.findVirtual;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;

/**
 * Factory for method handle proxies.
 *
 * Unlike {@link java.lang.invoke.MethodHandleProxies}, each proxy created by
 * this factory is implemented with its own class, and so serves as an inlining
 * and specialization point for the JVM.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 10/14/2014
 */
public final class ProxyFactory {
	private final Module module;
	private final ModuleClassLoader loader;
	private final String pkg;
	public ProxyFactory(ModuleClassLoader loader) {
		this.loader = requireNonNull(loader);
		this.module = this.loader.getModule();
		this.pkg = null;
	}

	public ProxyFactory(ModuleClassLoader loader, String pkg) {
		this.loader = requireNonNull(loader);
		this.module = this.loader.getModule();
		this.pkg = pkg.endsWith(".") ? pkg : pkg + ".";
	}

	public <T> T createProxy(String name, Map<String, MethodHandle> methods, Class<T> iface) {
		return iface.cast(createProxy(name, methods, Collections.singletonList(iface)));
	}

	public <T> T createProxy(String name, Map<String, MethodHandle> methods, Class<T> iface, Class<?>... moreIfaces) {
		return iface.cast(createProxy(name, methods, Lists.asList(iface, moreIfaces)));
	}

	public Object createProxy(String name, Map<String, MethodHandle> methods, List<Class<?>> ifaces) {
		List<Klass> interfaces = ifaces.stream()
				.map(module::getKlass)
				.collect(Collectors.toList());
		return createProxy0(name, methods, interfaces, false);
	}

	public MethodHandle bytecodify(MethodHandle handle, String className) {
		//TODO: this is making a nonstatic method, but we explicitly don't care
		//about the receiver.
		//The proxy class must be public so we can make handles to its methods.
		Object proxy = createProxy0(className, Collections.singletonMap("$invoke", handle), Collections.emptyList(), true);
		return findVirtual(proxy.getClass(), "$invoke").bindTo(proxy);
	}

	private Object createProxy0(String proxyName, Map<String, MethodHandle> methods, List<Klass> interfaces, boolean publicKlass) {
		checkArgument(module.klasses().containsAll(interfaces));
		Klass proxy = new Klass(pkg + proxyName, module.getKlass(Object.class), interfaces,
				EnumSet.of(Modifier.FINAL), module);
		if (publicKlass) proxy.setAccess(Access.PUBLIC);
		Methods.createDefaultConstructor(proxy);
		Methods.staticFinalFieldInitializer(proxy, methods);
		methods.forEach((name, handle) -> Methods.invokeExactFromField(proxy, proxy.getField(name),
				EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), name, handle.type()));

		try {
			Class<?> proxyClass = loader.loadClass(proxy.getName());
			//can't use proxyClass.newInstance() because it's not public
			Constructor<?> ctor = proxyClass.getConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}
}

package sk.nociar.jpacloner.properties;

import java.lang.reflect.Method;

public class MethodPropertyReader implements PropertyReader {
	
	private final Method getter;

	public MethodPropertyReader(Method getter) {
		this.getter = getter;
		getter.setAccessible(true);
	}

	@Override
	public Object get(Object instance) {
		try {
			return getter.invoke(instance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

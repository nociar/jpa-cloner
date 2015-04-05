package sk.nociar.jpacloner.properties;

import java.lang.reflect.Method;

public class MethodPropertyWriter implements PropertyWriter {
	
	private final Method setter;

	public MethodPropertyWriter(Method setter) {
		this.setter = setter;
		setter.setAccessible(true);
	}

	@Override
	public void set(Object instance, Object value) {
		try {
			setter.invoke(instance, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

package sk.nociar.jpacloner.properties;

import java.lang.reflect.Field;

public class FieldPropertyWriter implements PropertyWriter {

	private final Field field;

	public FieldPropertyWriter(Field field) {
		this.field = field;
		field.setAccessible(true);
	}
	
	@Override
	public void set(Object instance, Object value) {
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}

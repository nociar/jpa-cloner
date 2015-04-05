package sk.nociar.jpacloner.properties;

import java.lang.reflect.Field;

public class FieldPropertyReader implements PropertyReader {
	
	private final Field field;

	public FieldPropertyReader(Field field) {
		this.field = field;
		field.setAccessible(true);
	}

	@Override
	public Object get(Object instance) {
		try {
			return field.get(instance);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}

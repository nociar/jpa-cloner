package sk.nociar.jpacloner;

import static java.util.Collections.unmodifiableList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import sk.nociar.jpacloner.properties.FieldPropertyReader;
import sk.nociar.jpacloner.properties.FieldPropertyWriter;
import sk.nociar.jpacloner.properties.MethodPropertyReader;
import sk.nociar.jpacloner.properties.MethodPropertyWriter;
import sk.nociar.jpacloner.properties.PropertyReader;
import sk.nociar.jpacloner.properties.PropertyWriter;

/**
 * Info about JPA class. The class info also considers the {@link AccessType} of an {@link Entity} or {@link Embeddable} class.
 * For more information about the {@link AccessType} handling, please see 
 * <a href="http://docs.jboss.org/hibernate/orm/4.2/manual/en-US/html_single/#d5e3119">Hibernate documentation</a>.
 * 
 * @author Miroslav Nociar
 */
public class JpaClassInfo {
	private final Constructor<?> constructor;
	private final Map<String, Field> fields = new HashMap<String, Field>();
	private final Map<String, Method> getters = new HashMap<String, Method>();
	private final Map<String, Method> setters = new HashMap<String, Method>();
	/** Holds all JPA properties (basic and relations) */
	private final Map<String, JpaPropertyInfo> jpaProperties = new HashMap<String, JpaPropertyInfo>();
	private final List<String> properties;
	private final List<String> relations;
	
	private static final ConcurrentMap<Class<?>, JpaClassInfo> classInfo = new ConcurrentHashMap<Class<?>, JpaClassInfo>();
	
	public static JpaClassInfo get(Class<?> clazz) {
		clazz = getJpaClass(clazz);
		if (clazz == null) {
			return null;
		}
		JpaClassInfo info = classInfo.get(clazz);
		if (info == null) {
			// create information for the class
			info = new JpaClassInfo(clazz);
			classInfo.putIfAbsent(clazz, info);
		}
		return info;
	}

	/**
	 * Returns the raw JPA class (i.e. annotated by {@link Entity} or {@link Embeddable}) or <code>null</code>.
	 */
	public static Class<?> getJpaClass(Class<?> c) {
		while (c != null) {
			if (c.getAnnotation(Entity.class) != null || c.getAnnotation(Embeddable.class) != null) {
				return c;
			}
			c = c.getSuperclass();
		}
		return null;
	}


	private JpaClassInfo(final Class<?> clazz) {
		// find default constructor
		try {
			constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Unable to find default constructor for class: " + clazz, e);
		}
		// scan for all fields, getters and setters
		process(clazz);
		// determine the default access type
		AccessType accessType = null;
		for (Class<?> c = clazz; accessType == null && c != null; c = c.getSuperclass()) {
			Access access = clazz.getAnnotation(Access.class);
			if (access != null) {
				accessType = access.value();
			}
		}
		if (accessType == null) {
			// try to find @Id or @EmbeddedId in fields
			for (Field f : fields.values()) {
				if (f.getAnnotation(Id.class) != null || f.getAnnotation(EmbeddedId.class) != null) {
					accessType = AccessType.FIELD;
					break;
				}
			}
		}
		if (accessType == null) {
			// use the PROPERTY access type
			accessType = AccessType.PROPERTY;
		}
		// scan all getters
		for (String propertyName : getters.keySet()) {
			Method getter = getters.get(propertyName);
			Method setter = setters.get(propertyName);
			// determine access type
			AccessType ac = accessType;
			Access access = getter.getAnnotation(Access.class);
			if (access != null) {
				ac = access.value();
			}
			
			if (ac == AccessType.PROPERTY && setter != null) {
				JpaPropertyInfo i = new JpaPropertyInfo(getter, new MethodPropertyReader(getter), new MethodPropertyWriter(setter));
				jpaProperties.put(propertyName, i);
			}
		}
		// scan all fields
		for (String propertyName : fields.keySet()) {
			Field field = fields.get(propertyName);
			// determine access type
			AccessType ac = accessType;
			Access access = field.getAnnotation(Access.class);
			if (access != null) {
				ac = access.value();
			}
			if (ac != AccessType.FIELD) {
				continue;
			}
			Method getter = getters.get(propertyName);
			Method setter = setters.get(propertyName);
			// property reader
			final PropertyReader propertyReader;
			if (getter != null) {
				propertyReader = new MethodPropertyReader(getter);
			} else {
				propertyReader = new FieldPropertyReader(field);
			}
			// property writer
			final PropertyWriter propertyWriter;
			if (setter != null) {
				propertyWriter = new MethodPropertyWriter(setter);
			} else {
				propertyWriter = new FieldPropertyWriter(field);
			}
			
			jpaProperties.put(propertyName, new JpaPropertyInfo(field, propertyReader, propertyWriter));
		}
		// find all properties end relations
		List<String> properties = new ArrayList<String>();
		LinkedList<String> relations = new LinkedList<String>();
		
		for (Map.Entry<String, JpaPropertyInfo> entry : jpaProperties.entrySet()) {
			final String propertyName = entry.getKey();
			final JpaPropertyInfo propertyInfo = entry.getValue();
			
			if (propertyInfo.isBasic()) {
				properties.add(propertyName);
			} else {
				OneToMany oneToMany = propertyInfo.getAccessibleObject().getAnnotation(OneToMany.class);
				ManyToMany manyToMany = propertyInfo.getAccessibleObject().getAnnotation(ManyToMany.class);
				
				if (oneToMany == null && manyToMany == null) {
					relations.addLast(propertyName);
				} else {
					// optimization for *ToMany : putting in front may reduce DB queries
					relations.addFirst(propertyName);
				}
			}
		}
		
		this.properties = unmodifiableList(properties);
		this.relations = unmodifiableList(new ArrayList<String>(relations));
	}

	/**
	 * Process the class hierarchy.
	 */
	private void process(final Class<?> clazz) {
		if (clazz == null || clazz == Object.class || clazz.isInterface()) {
			return;
		}
		// process super class first
		process(clazz.getSuperclass());
		// fields
		for (Field f : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers())) {
				continue;
			}
			fields.put(f.getName(), f);
		}
		// getters & setters
		for (Method m : clazz.getDeclaredMethods()) {
			if (Modifier.isStatic(m.getModifiers())) {
				continue;
			}
			String methodName = m.getName();
			String propertyName = null;
			Map<String, Method> map = null;
			if (methodName.startsWith("get") && methodName.length() > 3 && m.getParameterTypes().length == 0) {
				propertyName = methodName.substring(3);
				map = getters;
			} else if (methodName.startsWith("is") && methodName.length() > 2 && m.getParameterTypes().length == 0) {
				propertyName = methodName.substring(2);
				map = getters;
			} else if (methodName.startsWith("set") && methodName.length() > 3 && m.getParameterTypes().length == 1) {
				propertyName = methodName.substring(3);
				map = setters;
			}
			if (propertyName != null && !propertyName.isEmpty()) {
				propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
				map.put(propertyName, m);
			}
		}
	}
	
	public Constructor<?> getConstructor() {
		return constructor;
	}

	public List<String> getProperties() {
		return properties;
	}

	public List<String> getRelations() {
		return relations;
	}

	public JpaPropertyInfo getPropertyInfo(String property) {
		return jpaProperties.get(property);
	}
}
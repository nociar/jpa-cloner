/*   JPA cloner project.
 *   
 *   Copyright (C) 2013 Miroslav Nociar
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package sk.nociar.jpacloner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import sk.nociar.jpacloner.graphs.EntityIntrospector;

/**
 * JPA entity introspector. JPA entities must use <b>field access</b>, not property access.
 * 
 * @author Miroslav Nociar
 */
public class JpaIntrospector implements EntityIntrospector {
	private JpaIntrospector() {
	}
	
	public static final JpaIntrospector INSTANCE = new JpaIntrospector();
	
	/**
	 * Info about JPA class.
	 * 
	 * @author Miroslav Nociar
	 */
	public static class JpaClassInfo {
		private final Constructor<?> constructor;
		private final List<String> properties;
		private final List<String> relations;
		private final Map<String, Field> fields = new HashMap<String, Field>();
		private final Map<String, Method> getters = new HashMap<String, Method>();
		private final Map<String, Method> setters = new HashMap<String, Method>();
		private final Map<String, List<String>> mappedBy = new HashMap<String, List<String>>();
		private final Method cloner;
		
		private JpaClassInfo(Class<?> clazz) {
			try {
				// find default constructor
				constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Unable to find default constructor for class: " + clazz, e);
			}
			Method cloner = null;
			// getters & setters
			for (Method m : clazz.getMethods()) {
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
				} else if ("clone".equals(methodName) && Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0) {
					cloner = m;
				}
				if (propertyName != null && !propertyName.isEmpty()) {
					propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
					map.put(propertyName, m);
				}
			}
			// fields
			List<String> properties = new ArrayList<String>();
			LinkedList<String> relations = new LinkedList<String>();
			for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
				for (Field f : c.getDeclaredFields()) {
					if (Modifier.isStatic(f.getModifiers())) {
						continue;
					}
					String name = f.getName();
					// note: this is called by hibernate anyway...
					f.setAccessible(true);
					// add to maps
					fields.put(name, f);
					if (!isRelation(f)) {
						properties.add(name);
					} else {
						OneToOne oneToOne = f.getAnnotation(OneToOne.class);
						String mappedName = null;
						if (oneToOne != null) {
							// OneToOne - add the relation to the end
							relations.addLast(name);
							mappedName = oneToOne.mappedBy();
						} else {
							// OneToMany/ManyToMany - insert the relation to the beginning
							// NOTE this may significantly decrease the number of queries during the cloning! 
							relations.addFirst(name);
							OneToMany oneToMany = f.getAnnotation(OneToMany.class);
							if (oneToMany != null) {
								mappedName = oneToMany.mappedBy();
							}
						}
						if (mappedName != null && !mappedName.trim().isEmpty()) {
							// OneToMany/OneToOne - handle the mappedBy attribute 
							mappedName = mappedName.trim();
							// NOTE: the mappedBy attribute may be used in @Embeddable
							if (mappedName.contains(".")) {
								mappedBy.put(name, unmodifiableList(asList(mappedName.split("\\."))));
							} else {
								mappedBy.put(name, singletonList(mappedName));
							}
						}
					}
					// check if the field has corresponding getter
					if (!getters.containsKey(name)) {
						throw new IllegalStateException("The class: " + clazz + " does not have a getter for field: " + name);
					}
				}
			}
			this.properties = unmodifiableList(properties);
			this.relations = unmodifiableList(new ArrayList<String>(relations));
			this.cloner = cloner;
		}
		
		private boolean isRelation(Field f) {
			return f.getAnnotation(ManyToOne.class) != null ||
					f.getAnnotation(OneToOne.class) != null ||
					f.getAnnotation(OneToMany.class) != null ||
					f.getAnnotation(ManyToMany.class) != null ||
					f.getAnnotation(Embedded.class) != null ||
					f.getAnnotation(EmbeddedId.class) != null ||
					f.getAnnotation(ElementCollection.class) != null;
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

		public Field getField(String property) {
			return fields.get(property);
		}

		public Method getGetter(String property) {
			return getters.get(property);
		}
		
		public Method getSetter(String property) {
			return setters.get(property);
		}

		public List<String> getMappedBy(String property) {
			return mappedBy.get(property);
		}
		
		public Method getCloner() {
			return cloner;
		}
	}
	
	private static final ConcurrentMap<Class<?>, JpaClassInfo> classInfo = new ConcurrentHashMap<Class<?>, JpaClassInfo>();
	
	public static JpaClassInfo getClassInfo(Object object) {
		if (object == null) {
			return null;
		}
		Class<?> clazz = getJpaClass(object);
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
	public static Class<?> getJpaClass(Object object) {
		if (object == null) {
			return null;
		}
		for (Class<?> c = object.getClass(); c != null; c = c.getSuperclass()) {
			if (c.getAnnotation(Entity.class) != null || c.getAnnotation(Embeddable.class) != null) {
				return c;
			}
		}
		return null;
	}
	
	public static Object getProperty(Object object, String property) {
		Method getter = getClassInfo(object).getGetter(property);
		try {
			return getter.invoke(object);
		} catch (Exception e) {
			throw new RuntimeException("Invocation problem object: " + object + ", property: " + property, e);
		}
	}

	public static void setProperty(Object object, String property, Object value) {
		JpaClassInfo info = getClassInfo(object);
		Method setter = info.getSetter(property);
		if (setter != null) {
			try {
				setter.invoke(object, value);
			} catch (Exception e) {
				throw new RuntimeException("Invocation problem object: " + object + ", property: " + property, e);
			}
		} else {
			// no setter, try field access
			Field field = info.getField(property);
			try {
				field.set(object, value);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static final List<String> mapEntryProperties = unmodifiableList(asList("key", "value"));
	
	@Override
	public Collection<String> getProperties(Object object) {
		if (object instanceof Entry) {
			return mapEntryProperties;
		}

		JpaClassInfo info = getClassInfo(object);
		return info == null ? null : info.getRelations();
	}

}

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


/**
 * JpaCloner provides cloning of JPA entity subgraphs. The subgraphs are defined by string patterns,
 * see {@link GraphExplorer} for more information. Usage examples:<br/><br/>
 * <code>
 * Project clonedProject = JpaCloner.clone(project, "devices.interfaces", "users.privileges");<br/>
 * School clonedSchool = JpaCloner.clone(school, "teachers.lessonToPupils.(key.class|value.parents)");<br/>
 * Node clonedNode = JpaCloner.clone(node, "(children.child)*.(nodeType|attributes.attributeType)");<br/>
 * Company clonedCompany = JpaCloner.clone(company, "department+.(boss|employees).address.(country|city|street)");<br/>
 * </code>
 * <ul>
 * <li>JPA entities must <b>correctly</b> implement the {@link Object#equals(Object obj)} 
 * method and the {@link Object#hashCode()} method!</li>
 * <li>JPA entities must use <b>field access</b>, not property access. 
 * Each field must have a corresponding <b>getter</b>.</li>
 * <li>Cloned entities will be instantiated as <b>raw classes</b>, not Hibernate proxy classes.
 * Raw classes means classes annotated by {@link Entity} or {@link Embeddable}.</li>
 * <li>Cloned entities will have all basic properties (i.e. columns) populated by default. 
 * Advanced control over the basic property cloning is supported via the {@link JpaPropertyFilter}.</li>
 * <li>Relations to neighboring entities will be populated <b>only</b> if specified by the string patterns, <code>null</code> otherwise.</li>
 * <li>Cloned collections and maps will be the standard java.util classes:
 * <table>
 * <tr><th>Original</th><th></th><th>Clone</th></tr>
 * <tr><td>{@link List}</td><td>-&gt;</td><td>{@link ArrayList}</td></tr>
 * <tr><td>{@link SortedSet}</td><td>-&gt;</td><td>{@link TreeSet}</td></tr>
 * <tr><td>{@link Set}</td><td>-&gt;</td><td>{@link HashSet}</td></tr>
 * <tr><td>{@link SortedMap}</td><td>-&gt;</td><td>{@link TreeMap}</td></tr>
 * <tr><td>{@link Map}</td><td>-&gt;</td><td>{@link HashMap}</td></tr>
 * </table>
 * </li>
 * <li>Cloning of {@link Map} supports navigation via "key" and "value" properties e.g. "my.map.(key.a.b.c|value.x.y.z)"</li>
 * <li>A JpaCloner instance is NOT thread safe; prefer the usage of static clone(...) methods, which are.</li>
 * </ul>
 * Please note that the cloning has also a side effect regarding the lazy loading. 
 * All entities which will be cloned must be fetched from the DB. It is advisable
 * (but not required) to perform the cloning inside a <b>transaction scope</b>.
 * 
 * @author Miroslav Nociar
 */
public class JpaCloner implements EntityExplorer {

	private final Map<Object, Object> originalToClone = new HashMap<Object, Object>();
	private final Map<Object, Map<String, Collection<Object>>> exploredCache = new HashMap<Object, Map<String, Collection<Object>>>();
	
	private final JpaPropertyFilter propertyFilter;
	
	private static final JpaPropertyFilter defaultPropertyFilter = new JpaPropertyFilter() {
		@Override
		public boolean isCloned(Object entity, String property) {
			return true;
		}
	};
	
	/**
	 * Info about JPA class.
	 * 
	 * @author Miroslav Nociar
	 */
	private static class JpaClassInfo {
		final Constructor<?> constructor;
		final List<String> columns = new ArrayList<String>();
		final Map<String, Field> fields = new HashMap<String, Field>();
		final Map<String, Method> getters = new HashMap<String, Method>();
		final Map<String, String> mappedBy = new HashMap<String, String>();
		
		JpaClassInfo(Class<?> clazz) {
			if (clazz != getJpaClass(clazz)) {
				throw new IllegalArgumentException("Not a JPA class: " + clazz);
			}
			// find default constructor
			try {
				constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Unable to find default constructor for class: " + clazz, e);
			}
			// getters
			for (Method m : clazz.getMethods()) {
				if (Modifier.isStatic(m.getModifiers())) {
					continue;
				}
				String methodName = m.getName();
				String propertyName = null;
				if (methodName.startsWith("get") && methodName.length() > 3) {
					propertyName = methodName.substring(3);
				} else if (methodName.startsWith("is") && methodName.length() > 2) {
					propertyName = methodName.substring(2);
				}
				if (propertyName != null && !propertyName.isEmpty()) {
					propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
					getters.put(propertyName, m);
				}
			}
			// fields
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
					if (isBaseProperty(f)) {
						columns.add(name);
					} else {
						OneToMany oneToMany = f.getAnnotation(OneToMany.class);
						if (oneToMany != null) {
							String mappedName = oneToMany.mappedBy();
							if (mappedName != null && !mappedName.trim().isEmpty()) {
								mappedBy.put(name, mappedName);
							}
						}
					}
					// check if the field has corresponding getter
					if (!getters.containsKey(name)) {
						throw new IllegalStateException("The class: " + clazz + " does not have a getter for field: " + name);
					}
				}
			}
		}
	}
	
	private static final Map<Class<?>, JpaClassInfo> classInfo = new ConcurrentHashMap<Class<?>, JpaClassInfo>();
	
	private static boolean isBaseProperty(Field f) {
		return f.getAnnotation(ManyToOne.class) == null &&
				f.getAnnotation(OneToOne.class) == null &&
				f.getAnnotation(OneToMany.class) == null &&
				f.getAnnotation(ManyToMany.class) == null &&
				f.getAnnotation(Embedded.class) == null &&
				f.getAnnotation(ElementCollection.class) == null;
	}
	
	private static JpaClassInfo getClassInfo(Class<?> clazz) {
		JpaClassInfo info = classInfo.get(clazz);
		if (info == null) {
			// create information for the class
			info = new JpaClassInfo(clazz);
			classInfo.put(clazz, info);
		}
		return info;
	}

	/**
	 * Returns the raw JPA class for a clone (not hibernate proxy) or <code>null</code>.
	 * @param clazz
	 * @return
	 */
	public static Class<?> getJpaClass(Class<?> clazz) {
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			if (c.getAnnotation(Entity.class) != null || c.getAnnotation(Embeddable.class) != null) {
				return c;
			}
		}
		return null;
	}
	
	private static Object getProperty(Object object, String property) {
		Class<?> jpaClass = getJpaClass(object.getClass());
		Method m = getClassInfo(jpaClass).getters.get(property);
		try {
			return m.invoke(object);
		} catch (Exception e) {
			throw new IllegalStateException("Invocation problem object: " + object + ", property: " + property, e);
		}
	}

	private static void setProperty(Object object, String property, Object value) {
		Class<?> jpaClass = getJpaClass(object.getClass());
		Field field = getClassInfo(jpaClass).fields.get(property);
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Set problem.", e);
		}
	}
	
	public JpaCloner() {
		this(defaultPropertyFilter);
	}
	
	public JpaCloner(JpaPropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
	}
	
	/**
	 * Returns <code>true</code> if the original object has cloneable property (relation).
	 */
	private boolean isCloneable(Object original, String property) {
		Class<?> jpaClass = getJpaClass(original.getClass());
		if (jpaClass == null) {
			return false;
		}
		JpaClassInfo info = getClassInfo(jpaClass);
		
		return info.fields.containsKey(property) && info.getters.containsKey(property) && !info.columns.contains(property);
	}
	
	/**
	 * Returns the mappedBy for the property or <code>null</code>.  
	 */
	private String getMappedBy(Object original, String property) {
		Class<?> jpaClass = getJpaClass(original.getClass());
		if (jpaClass == null) {
			return null;
		}
		return getClassInfo(jpaClass).mappedBy.get(property);
	}
	
	@Override
	public Collection<Object> explore(Object original, String property) {
		if (original == null) {
			return null;
		}
		Map<String, Collection<Object>> propertyToExplored = exploredCache.get(original);
		if (propertyToExplored  == null) {
			propertyToExplored = new HashMap<String, Collection<Object>>();
			exploredCache.put(original, propertyToExplored);
		}
		Collection<Object> explored = propertyToExplored.get(property);
		if (explored == null) {
			explored = exploreAndClone(original, property);
			propertyToExplored.put(property, explored);
		}
		return explored;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<Object> exploreAndClone(Object original, String property) {
		if (original instanceof Entry) {
			Entry entry = (Entry) original;
			// handle Map.Entry#getKey() and Map.Entry#getValue()
			if ("key".equals(property)) {
				return Collections.singleton(entry.getKey());
			} else if ("value".equals(property)) {
				return Collections.singleton(entry.getValue());
			} else {
				throw new IllegalArgumentException("Map.Entry does not have property: " + property);
			}
		}
		
		if (!isCloneable(original, property)) {
			return null;
		}
			
		Object clone = getClone(original);
		Object value = getProperty(original, property);

		if (value == null) {
			return null;
		}

		String mappedBy = getMappedBy(original, property);
		Object clonedValue;
		Collection explored;
		
		if (value instanceof Collection) {
			// Collection property
			explored = (Collection) value;
			Collection clonedCollection;
			if (explored instanceof SortedSet) {
				// create a tree set with the same comparator (may be null)
				clonedCollection = new TreeSet(((SortedSet) explored).comparator());
			} else if (explored instanceof Set) {
				// create a hash set
				clonedCollection = new HashSet();
			} else if (explored instanceof List) {
				// create an array list
				clonedCollection = new ArrayList(explored.size());
			} else {
				throw new IllegalArgumentException("Unsupported collection class: " + explored.getClass());
			}
			for (Object o : explored) {
				Object c = getClone(o);
				if (mappedBy != null && c != o) {
					// handle OneToMany#mappedBy()
					setProperty(c, mappedBy, clone);
				}
				clonedCollection.add(c);
			}
			clonedValue = clonedCollection;
		} else if (value instanceof Map) {
			// Map property
			Set<Entry<Object, Object>> entries = ((Map<Object, Object>) value).entrySet(); 
			explored = (Collection) entries;
			Map clonedMap;
			if (value instanceof SortedMap) {
				clonedMap = new TreeMap(((SortedMap) value).comparator());
			} else {
				clonedMap = new HashMap();
			}
			for (Entry<Object, Object> entry : entries) {
				Object mapKey = getClone(entry.getKey());
				Object mapValue = getClone(entry.getValue());
				if (mappedBy != null && mapValue != entry.getValue()) {
					// handle OneToMany#mappedBy()
					setProperty(mapValue, mappedBy, clone);
				}
				clonedMap.put(mapKey, mapValue);
			}
			clonedValue = clonedMap;
		} else {
			// singular property
			explored = Collections.singleton(value);
			clonedValue = getClone(value);
		}
		
		setProperty(clone, property, clonedValue);

		return explored;
	}
	
	public Object getClone(Object original) {
		if (original == null) {
			return null;
		}
		Class<?> jpaClass = getJpaClass(original.getClass());
		if (jpaClass == null) {
			// do not clone, return the original entity (e.g. @ElementCollection)
			return original;
		}
		// check the cache first
		Object clone = originalToClone.get(original);
		if (clone != null) {
			return clone;
		}
		JpaClassInfo classInfo = getClassInfo(jpaClass);
		try {
			clone = classInfo.constructor.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to clone: " + original, e);
		}
		// clone columns
		for (String property : classInfo.columns) {
			if (propertyFilter.isCloned(original, property)) {
				Object value = getProperty(original, property);
				setProperty(clone, property, value);
			}
		}
		// put in the cache
		originalToClone.put(original, clone);
		return clone;
	}

	public Map<Object, Object> getOriginalToClone() {
		return originalToClone;
	}
	
	private static final Map<String, GraphExplorer> patternToExplorer = new ConcurrentHashMap<String, GraphExplorer>();
	
	private static GraphExplorer getExplorer(String pattern) {
		GraphExplorer explorer = patternToExplorer.get(pattern);
		if (explorer == null) {
			explorer = new GraphExplorer(pattern);
			patternToExplorer.put(pattern, explorer);
		}
		return explorer;
	}

	@SuppressWarnings("unchecked")
	private static <T> T clone(T root, JpaCloner jpaCloner, String... patterns) {
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer explorer = getExplorer(pattern);
				explorer.explore(root, jpaCloner);
			}
		}
		return (T) jpaCloner.getClone(root);
	}

	private static <T> void cloneCollection(Collection<T> originalCollection, Collection<T> clonedCollection, JpaPropertyFilter propertyFilter, String... patterns) {
		JpaCloner jpaCloner = new JpaCloner(propertyFilter);
		for (T root : originalCollection) {
			clonedCollection.add(clone(root, jpaCloner, patterns));
		}
	}

	/**
	 * Clones the passed JPA entity, for description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> T clone(T root, JpaPropertyFilter propertyFilter, String... patterns) {
		return clone(root, new JpaCloner(propertyFilter), patterns);
	}

	/**
	 * Clones the list of JPA entities, for description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> List<T> clone(List<T> list, JpaPropertyFilter propertyFilter, String... patterns) {
		List<T> clonedList = new ArrayList<T>(list.size());
		cloneCollection(list, clonedList, propertyFilter, patterns);
		return clonedList;
	}

	/**
	 * Clones the set of JPA entities, for description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> Set<T> clone(Set<T> set, JpaPropertyFilter propertyFilter, String... patterns) {
		Set<T> clonedSet = new HashSet<T>();
		cloneCollection(set, clonedSet, propertyFilter, patterns);
		return clonedSet;
	}

	/**
	 * Clones the passed JPA entity, for description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> T clone(T root, String... patterns) {
		return clone(root, defaultPropertyFilter, patterns);
	}

	/**
	 * Clones the list of JPA entities, for description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> List<T> clone(List<T> list, String... patterns) {
		return clone(list, defaultPropertyFilter, patterns);
	}

	/**
	 * Clones the set of JPA entities, for description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> Set<T> clone(Set<T> set, String... patterns) {
		return clone(set, defaultPropertyFilter, patterns);
	}	
}

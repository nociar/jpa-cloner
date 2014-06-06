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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

import sk.nociar.jpacloner.graphs.GraphExplorer;
import sk.nociar.jpacloner.graphs.PropertyFilter;

/**
 * JpaCloner provides cloning of JPA entity subgraphs. Cloned entities will be instantiated as <b>raw classes</b>.
 * The <b>raw class</b> means a class annotated by {@link Entity} or {@link Embeddable}, not a Hibernate proxy. 
 * String patterns define <b>included relations</b> which will be cloned. For description of patterns see the {@link GraphExplorer}.
 * Cloned entities will have all <b>basic properties</b> (non-relation properties) copied by default.
 * Advanced control over the cloning process is supported via the {@link PropertyFilter} interface.
 * There are two options for cloning:<br/><br/>
 * <ol>
 * <li> 
 * Cloning without a {@link PropertyFilter}. All <b>basic properties</b> of entities are copied by default in this case:
 * <pre>
 * Company cloned = JpaCloner.clone(company, "department+.(boss|employees).address");</pre>
 * </li>
 * <li>
 * Cloning with a {@link PropertyFilter}. The {@link PropertyFilter} implementation serves as an exclusion filter 
 * of <b>relations</b> and <b>basic properties</b>:
 * <pre>
 * PropertyFilter filter = new PropertyFilter() {
 *     public boolean test(Object entity, String property) {
 *         // do not clone primary keys
 *         return !"id".equals(property);
 *     }
 * } 
 * Company cloned = JpaCloner.clone(company, filter, "department+.(boss|employees).address");</pre>
 * </li>
 * </ol>
 * Cloned <b>relations</b> will be standard java.util classes:<br/>
 * {@link Set}-&gt;{@link LinkedHashSet}<br/>
 * {@link Map}-&gt;{@link LinkedHashMap}<br/>
 * {@link List}-&gt;{@link ArrayList}<br/>
 * {@link SortedSet}-&gt;{@link TreeSet}<br/>
 * {@link SortedMap}-&gt;{@link TreeMap}<br/>
 * <br/>
 * Cloning of a {@link Map} is supported via "key" and "value" properties e.g. "my.map.(key.a.b.c|value.x.y.z)".
 * Please note that the cloning has also a side effect regarding the lazy loading. 
 * All entities which will be cloned could be fetched from the DB. It is advisable
 * (but not required) to perform the cloning inside a <b>transaction scope</b>.
 * <br/><br/>
 * Requirements:
 * <ul>
 * <li>JPA entities must use <b>field access</b>, not property access.</li>
 * <li>JPA entities must <b>correctly</b> implement the {@link Object#equals(Object obj)} 
 * method and the {@link Object#hashCode()} method!</li>
 * </ul>
 * 
 * @author Miroslav Nociar
 */
public class JpaCloner extends AbstractJpaExplorer {

	private final Map<Object, Object> originalToClone = new HashMap<Object, Object>();
	
	public JpaCloner() {
		super();
	}
	
	public JpaCloner(PropertyFilter propertyFilter) {
		super(propertyFilter);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void explore(Object entity, String property, Collection<?> collection) {
		Collection clonedCollection;
		if (collection instanceof SortedSet) {
			// create a tree set with the same comparator (may be null)
			clonedCollection = new TreeSet(((SortedSet) collection).comparator());
		} else if (collection instanceof Set) {
			// create a hash set
			clonedCollection = new LinkedHashSet();
		} else if (collection instanceof List) {
			// create an array list
			clonedCollection = new ArrayList(collection.size());
		} else {
			throw new IllegalArgumentException("Unsupported collection class: " + collection.getClass());
		}
		for (Object o : collection) {
			clonedCollection.add(getClone(o));
		}
		setProperty(getClone(entity), property, clonedCollection);
		// handle mappedBy
		List<String> mappedBy = getClassInfo(entity).getMappedBy(property);
		if (mappedBy != null) {
			for (Object value : collection) {
				handleMappedBy(value, mappedBy, 0);
			}
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void explore(Object entity, String property, Map<?, ?> map) {
		Map clonedMap;
		if (map instanceof SortedMap) {
			clonedMap = new TreeMap(((SortedMap) map).comparator());
		} else {
			clonedMap = new LinkedHashMap();
		}
		for (Entry entry : map.entrySet()) {
			clonedMap.put(getClone(entry.getKey()), getClone(entry.getValue()));
		}
		setProperty(getClone(entity), property, clonedMap);
		// handle mappedBy
		List<String> mappedBy = getClassInfo(entity).getMappedBy(property);
		if (mappedBy != null) {
			for (Object value : map.values()) {
				handleMappedBy(value, mappedBy, 0);
			}
		}
	}

	@Override
	protected void explore(Object entity, String property, Object value) {
		setProperty(getClone(entity), property, getClone(value));
		// handle mappedBy
		List<String> mappedBy = getClassInfo(entity).getMappedBy(property);
		if (mappedBy != null) {
			handleMappedBy(value, mappedBy, 0);
		}
	}
	
	private void handleMappedBy(Object o, List<String> mappedBy, int idx) {
		Collection<?> explored = explore(o, mappedBy.get(idx));
		idx++;
		if (explored != null && idx < mappedBy.size()) {
			for (Object e : explored) {
				handleMappedBy(e, mappedBy, idx);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getClone(T original) {
		if (original == null) {
			return null;
		}
		// check the cache first
		Object clone = originalToClone.get(original);
		if (clone != null) {
			return (T) clone;
		}
		JpaClassInfo classInfo = getClassInfo(original);
		if (classInfo == null) {
			// not a JPA class, return the original object
			return original;
		}
		try {
			clone = classInfo.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to clone: " + original, e);
		}
		// copy basic properties
		copyProperties(original, clone, classInfo, propertyFilter);
		// put in the cache
		originalToClone.put(original, clone);
		return (T) clone;
	}

	/**
	 * Clones the passed JPA entity. The property filter controls the cloning of <b>basic properties</b>. 
	 * The cloned relations are specified by string patters. For description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> T clone(T root, PropertyFilter propertyFilter, String... patterns) {
		JpaCloner cloner = new JpaCloner(propertyFilter);
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer explorer = GraphExplorer.get(pattern);
				explorer.explore(Collections.singleton(root), cloner);
			}
		}

		return cloner.getClone(root);
	}

	/**
	 * Clones the list of JPA entities. The property filter controls the cloning of <b>basic properties</b>. 
	 * The cloned relations are specified by string patters. For description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> List<T> clone(Collection<T> list, PropertyFilter propertyFilter, String... patterns) {
		List<T> clonedList = new ArrayList<T>(list.size());
		JpaCloner jpaCloner = new JpaCloner(propertyFilter);
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer explorer = GraphExplorer.get(pattern);
				explorer.explore(list, jpaCloner);
			}
		}
		for (T original : list) {
			clonedList.add(jpaCloner.getClone(original));
		}
		return clonedList;
	}

	/**
	 * Clones the set of JPA entities. The property filter controls the cloning of <b>basic properties</b>. 
	 * The cloned relations are specified by string patters. For description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> Set<T> clone(Set<T> set, PropertyFilter propertyFilter, String... patterns) {
		Set<T> clonedSet = new HashSet<T>();
		JpaCloner jpaCloner = new JpaCloner(propertyFilter);
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer explorer = GraphExplorer.get(pattern);
				explorer.explore(set, jpaCloner);
			}
		}
		for (T original : set) {
			clonedSet.add(jpaCloner.getClone(original));
		}
		return clonedSet;
	}

	/**
	 * Clones the passed JPA entity. Each entity has <b>all basic properties</b> cloned. 
	 * The cloned relations are specified by string patters. For description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> T clone(T root, String... patterns) {
		return clone(root, PropertyFilterFactory.getDefaultFilter(), patterns);
	}

	/**
	 * Clones the list of JPA entities. Each entity has <b>all basic properties</b> cloned. 
	 * The cloned relations are specified by string patters. For description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> List<T> clone(Collection<T> list, String... patterns) {
		return clone(list, PropertyFilterFactory.getDefaultFilter(), patterns);
	}

	/**
	 * Clones the set of JPA entities. Each entity has <b>all basic properties</b> cloned. 
	 * The cloned relations are specified by string patters. For description of patterns see the {@link GraphExplorer}.
	 */
	public static <T> Set<T> clone(Set<T> set, String... patterns) {
		return clone(set, PropertyFilterFactory.getDefaultFilter(), patterns);
	}

	/**
	 * Copy properties (not relations) from o1 to o2.
	 */
	private static void copyProperties(Object o1, Object o2, JpaClassInfo classInfo, PropertyFilter propertyFilter) {
		for (String property : classInfo.getProperties()) {
			if (propertyFilter.test(o1, property)) {
				Object value = AbstractJpaExplorer.getProperty(o1, property);
				AbstractJpaExplorer.setProperty(o2, property, value);
			}
		}
	}
	
	/**
	 * Copy all <b>basic properties</b> from the first entity to the second entity.
	 */
	public static <T, X extends T> void copy(T o1, X o2) {
		copy(o1, o2, PropertyFilterFactory.getDefaultFilter());
	}
	
	/**
	 * Copy filtered <b>basic properties</b> from the first entity to the second entity.
	 */
	public static <T, X extends T> void copy(T o1, X o2, PropertyFilter propertyFilter) {
		JpaClassInfo classInfo = AbstractJpaExplorer.getClassInfo(o1);
		if (classInfo != null) {
			copyProperties(o1, o2, classInfo, propertyFilter);
		}
	}

}

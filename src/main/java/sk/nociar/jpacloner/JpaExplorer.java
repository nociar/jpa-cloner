package sk.nociar.jpacloner;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import sk.nociar.jpacloner.graphs.EntityExplorer;
import sk.nociar.jpacloner.graphs.GraphExplorer;

/**
 * Generic explorer of JPA entities. Explored entities can be accessed by the method {@link #getEntities(Class)}.
 * 
 * @author Miroslav Nociar
 *
 */
public final class JpaExplorer implements EntityExplorer {
	
	final PropertyFilter propertyFilter;
	
	final Map<Object, Set<String>> entities = new HashMap<Object, Set<String>>();
	
	private JpaExplorer(PropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
	}
	
	private static final List<String> mapEntryProperties = unmodifiableList(asList("key", "value"));
	
	@Override
	public Collection<String> getProperties(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof Entry) {
			return mapEntryProperties;
		}
		JpaClassInfo info = JpaClassInfo.get(object.getClass());
		return info == null ? Collections.<String>emptyList() : info.getRelations();
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public final Collection<?> explore(Object entity, String property) {
		if (entity == null || property == null) {
			return null;
		}
		
		if (entity instanceof Entry) {
			Entry entry = (Entry) entity;
			// handle Map.Entry#getKey() and Map.Entry#getValue()
			if ("key".equals(property)) {
				return Collections.singleton(entry.getKey());
			} else if ("value".equals(property)) {
				return Collections.singleton(entry.getValue());
			} else {
				throw new IllegalArgumentException("Map.Entry does not have property: " + property);
			}
		}
		
		if (!propertyFilter.test(entity, property)) {
			return null;
		}
		
		JpaClassInfo classInfo = JpaClassInfo.get(entity.getClass());
		if (classInfo == null) {
			return null;
		}
		JpaPropertyInfo propertyInfo = classInfo.getPropertyInfo(property);
		if (propertyInfo == null || propertyInfo.isBasic()) {
			// explored property must be a relation
			return null;
		}
		addJpaObject(entity, property);

		final Object value = propertyInfo.getValue(entity);
		if (value == null) {
			return null;
		}
		
		final List<String> mappedBy = propertyInfo.getMappedBy();
		Collection<?> exploredObjects = null;
		if (value instanceof Collection) {
			// Collection property
			exploredObjects = (Collection) value;
			for (Object object : exploredObjects) {
				addJpaObject(object);
			}
			// handle mappedBy
			handleMappedBy(exploredObjects, mappedBy);
		} else if (value instanceof Map) {
			// Map property
			Map map = (Map) value;
			exploredObjects = map.entrySet();
			for (Object e : exploredObjects) {
				Entry entry = (Entry) e;
				addJpaObject(entry.getKey());
				addJpaObject(entry.getValue());
			}
			// handle mappedBy
			handleMappedBy(map.values(), mappedBy);
		} else {
			// singular property
			addJpaObject(value);
			exploredObjects = Collections.singleton(value);
			// handle mappedBy
			handleMappedBy(exploredObjects, mappedBy);
		}
		
		return exploredObjects;
	}
	
	private void handleMappedBy(Collection<?> objects, List<String> mappedBy) {
		if (mappedBy == null || mappedBy.isEmpty()) {
			return;
		}
		for (Object o : objects) {
			handleMappedBy(o, mappedBy, 0);
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
	
	private void addJpaObject(Object object) {
		if (object != null && JpaClassInfo.getJpaClass(object.getClass()) != null) {
			if (!entities.containsKey(object)) {
				entities.put(object, new HashSet<String>());
			}
		}
	}
	
	private void addJpaObject(Object object, String property) {
		if (object != null && JpaClassInfo.getJpaClass(object.getClass()) != null) {
			Set<String> properties = entities.get(object);
			if (properties == null) {
				properties = new HashSet<String>();
				entities.put(object, properties);
			}
			properties.add(property);
		}
	}
	
	/**
	 * Returns all explored entities of the given class.
	 * 
	 * @param clazz the entity class
	 * @return a set of explored entities of the given class
	 */
	public <T> Set<T> getEntities(Class<T> clazz) {
		Set<T> set = new HashSet<T>();
		for (Object entity : entities.keySet()) {
			if (clazz.isInstance(entity)) {
				set.add(clazz.cast(entity));
			}
		}
		return set;
	}

	/**
	 * Explores the passed JPA entity. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Object root, String... patterns) {
		return doExplore(root, PropertyFilters.getDefaultFilter(), patterns);
	}
	
	/**
	 * Explores the passed JPA entity. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Object root, PropertyFilter propertyFilter, String... patterns) {
		return doExplore(Collections.singleton(root), propertyFilter, patterns);
	}

	/**
	 * Explores a collection of JPA entities. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Collection<?> collection, String... patterns) {
		return doExplore(collection, PropertyFilters.getDefaultFilter(), patterns);
	}

	/**
	 * Explores a collection of JPA entities. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Collection<?> collection, PropertyFilter propertyFilter, String... patterns) {
		JpaExplorer jpaExplorer = new JpaExplorer(propertyFilter);
		for (Object root : collection) {
			jpaExplorer.addJpaObject(root);
		}
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer graphExplorer = GraphExplorer.get(pattern);
				graphExplorer.explore(collection, jpaExplorer);
			}
		}
		return jpaExplorer;
	}

}

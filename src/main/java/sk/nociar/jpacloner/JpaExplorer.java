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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sk.nociar.jpacloner.JpaIntrospector.JpaClassInfo;
import sk.nociar.jpacloner.graphs.EntityExplorer;
import sk.nociar.jpacloner.graphs.GraphExplorer;
import sk.nociar.jpacloner.graphs.PropertyFilter;

/**
 * Generic explorer of JPA entities. Explored entities can be accessed by the method {@link #getEntities(Class)}.
 * 
 * @author Miroslav Nociar
 *
 */
public class JpaExplorer implements EntityExplorer {
	
	private final Set<Object> entities = new HashSet<Object>();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection<Object> explore(Object entity, String property) {
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
		
		JpaClassInfo info = JpaIntrospector.getClassInfo(entity);
		if (info == null || !info.getRelations().contains(property)) {
			return null;
		}
		
		addJpaObject(entity);
		
		Object value = JpaIntrospector.getProperty(entity, property);

		if (value == null) {
			return null;
		}

		if (value instanceof Collection) {
			// Collection property
			Collection collection = (Collection) value;
			for (Object object : collection) {
				addJpaObject(object);	
			}
			return collection;
		} else if (value instanceof Map) {
			// Map property
			Map map = ((Map) value);
			for (Object object : map.keySet()) {
				addJpaObject(object);
			}
			for (Object object : map.values()) {
				addJpaObject(object);
			}
			return map.entrySet();
		} else {
			// singular property
			addJpaObject(value);
			return Collections.singleton(value);
		}
	}
	
	private void addJpaObject(Object object) {
		if (JpaIntrospector.getJpaClass(object) != null) {
			entities.add(object);
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
		for (Object entity : entities) {
			if (clazz.isInstance(entity)) {
				set.add(clazz.cast(entity));
			}
		}
		return set;
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
	
	private static void explore(Object root, JpaExplorer jpaExplorer, String... patterns) {
		jpaExplorer.addJpaObject(root);
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer graphExplorer = getExplorer(pattern);
				graphExplorer.explore(root, jpaExplorer);
			}
		}
	}
	
	/**
	 * Explores the passed JPA entity. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Object root, String... patterns) {
		JpaExplorer jpaExplorer = new JpaExplorer();
		explore(root, jpaExplorer, patterns);
		return jpaExplorer;
	}

	/**
	 * Explores a collection of JPA entities. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Collection<?> collection, String... patterns) {
		JpaExplorer jpaExplorer = new JpaExplorer();
		for (Object root : collection) {
			explore(root, jpaExplorer, patterns);
		}
		return jpaExplorer;
	}

	/**
	 * Explores the passed JPA entity by filter. The property filter controls
	 * the exploring of entity relations.
	 */
	public static JpaExplorer doExplore(Object root, PropertyFilter filter) {
		JpaExplorer jpaExplorer = new JpaExplorer();
		jpaExplorer.addJpaObject(root);
		GraphExplorer.explore(root, new HashSet<Object>(), jpaExplorer, JpaIntrospector.INSTANCE, filter);
		return jpaExplorer; 
	}
	
	/**
	 * Explores a collection of JPA entities by filter. The property filter controls
	 * the exploring of entity relations.
	 */
	public static JpaExplorer doExplore(Collection<?> collection, PropertyFilter filter) {
		JpaExplorer jpaExplorer = new JpaExplorer();
		Set<Object> exploredEntities = new HashSet<Object>();
		for (Object root : collection) {
			jpaExplorer.addJpaObject(root);
			GraphExplorer.explore(root, exploredEntities, jpaExplorer, JpaIntrospector.INSTANCE, filter);
		}
		return jpaExplorer;
	}

}

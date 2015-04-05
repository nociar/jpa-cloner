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
import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sk.nociar.jpacloner.graphs.EntityExplorer;
import sk.nociar.jpacloner.graphs.PropertyFilter;

/**
 * Simple explorer of JPA entities.
 * 
 * @author Miroslav Nociar
 */
public abstract class AbstractJpaExplorer implements EntityExplorer {
	
	protected final PropertyFilter propertyFilter;

	protected AbstractJpaExplorer() {
		this(PropertyFilters.getDefaultFilter());
	}
	
	protected AbstractJpaExplorer(PropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
	}
	
	private static final List<String> mapEntryProperties = unmodifiableList(asList("key", "value"));
	
	@Override
	public Collection<String> getProperties(Object object) {
		if (object instanceof Entry) {
			return mapEntryProperties;
		}

		JpaClassInfo info = getClassInfo(object);
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
		
		JpaClassInfo classInfo = getClassInfo(entity);
		if (classInfo == null) {
			return null;
		}
		JpaPropertyInfo propertyInfo = classInfo.getPropertyInfo(property);
		if (propertyInfo == null || propertyInfo.isBasic()) {
			// explored property must by relation
			return null;
		}

		Object value = propertyInfo.getPropertyReader().get(entity);

		if (value == null) {
			return null;
		} else if (value instanceof Collection) {
			// Collection property
			Collection collection = (Collection) value;
			explore(entity, propertyInfo, collection);
			return collection;
		} else if (value instanceof Map) {
			// Map property
			Map map = (Map) value;
			explore(entity, propertyInfo, map);
			return map.entrySet();
		}
		// singular property
		explore(entity, propertyInfo, value);
		return Collections.singleton(value);
	}
	
	protected abstract void explore(Object entity, JpaPropertyInfo property, Collection<?> collection);
	protected abstract void explore(Object entity, JpaPropertyInfo property, Map<?, ?> map);
	protected abstract void explore(Object entity, JpaPropertyInfo property, Object value);

	public static JpaClassInfo getClassInfo(Object object) {
		return object == null ? null : JpaClassInfo.get(object.getClass());
	}

}

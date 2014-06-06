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
package sk.nociar.jpacloner.graphs;

import java.util.Collection;

/**
 * Generic explorer of entities (graph nodes). Explored objects MUST correctly
 * implement the {@link Object#equals(Object)} method and the
 * {@link Object#hashCode()} method!
 * 
 * @author Miroslav Nociar
 * 
 */
public interface EntityExplorer {

	/**
	 * Explore a property of an entity, may return <code>null</code>.
	 * 
	 * @param entity
	 *            the entity (node)
	 * @param property
	 *            the property (edge)
	 * @return a collection of explored entities or <code>null</code>.
	 */
	public Collection<?> explore(Object entity, String property);
	
	/** 
	 * Returns a collection of properties for an entity, may not return the <code>null</code> value.
	 */
	public Collection<String> getProperties(Object entity);

}
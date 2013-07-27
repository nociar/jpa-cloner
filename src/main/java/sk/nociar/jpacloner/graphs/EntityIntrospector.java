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
 * Introspector of entities (graph nodes).
 * 
 * @author Miroslav Nociar
 */
public interface EntityIntrospector {
	
	/** 
	 * Returns a collection of properties for an object, may return the <code>null</code> value.
	 */
	public Collection<String> getProperties(Object object);

}

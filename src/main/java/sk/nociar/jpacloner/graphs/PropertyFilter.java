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

/**
 * Simple property filter. <br/>
 * <b>NOTE</b> this interface could be replaced by 
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/function/BiPredicate.html">BiPredicate</a>
 * after the Java 8 will be adopted.
 * 
 * @author Miroslav Nociar
 */
public interface PropertyFilter {
	/**
	 * Returns <code>true</code> if a property of an entity should be processed,
	 * <code>false</code> otherwise.
	 * 
	 * @param entity
	 *            the entity
	 * @param property
	 *            the property
	 */
	public boolean test(Object entity, String property);
}

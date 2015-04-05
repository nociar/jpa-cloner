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
package sk.nociar.jpacloner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sk.nociar.jpacloner.graphs.GraphExplorer;
import sk.nociar.jpacloner.graphs.PropertyFilter;

/**
 * Generic explorer of JPA entities. Explored entities can be accessed by the method {@link #getEntities(Class)}.
 * 
 * @author Miroslav Nociar
 *
 */
public class JpaExplorer extends AbstractJpaExplorer {
	
	private final Set<Object> entities = new HashSet<Object>();
	
	public JpaExplorer() {
		super();
	}
	
	public JpaExplorer(PropertyFilter propertyFilter) {
		super(propertyFilter);
	}
	
	@Override
	protected void explore(Object entity, JpaPropertyInfo property, Collection<?> collection) {
		for (Object object : collection) {
			addJpaObject(object);
		}
	}

	@Override
	protected void explore(Object entity, JpaPropertyInfo property, Map<?, ?> map) {
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			addJpaObject(entry.getKey());
			addJpaObject(entry.getValue());
		}
	}

	@Override
	protected void explore(Object entity, JpaPropertyInfo property, Object value) {
		addJpaObject(value);
	}

	private void addJpaObject(Object object) {
		if (getClassInfo(object) != null) {
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

	/**
	 * Explores the passed JPA entity. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Object root, String... patterns) {
		JpaExplorer jpaExplorer = new JpaExplorer();
		jpaExplorer.addJpaObject(root);
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer graphExplorer = GraphExplorer.get(pattern);
				graphExplorer.explore(Collections.singleton(root), jpaExplorer);
			}
		}
		return jpaExplorer;
	}
	
	/**
	 * Explores the passed JPA entity. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Object root, PropertyFilter propertyFilter, String... patterns) {
		JpaExplorer jpaExplorer = new JpaExplorer(propertyFilter);
		jpaExplorer.addJpaObject(root);
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer graphExplorer = GraphExplorer.get(pattern);
				graphExplorer.explore(Collections.singleton(root), jpaExplorer);
			}
		}
		return jpaExplorer;
	}

	/**
	 * Explores a collection of JPA entities. The explored relations are specified by string patters. 
	 * For description of patterns see the {@link GraphExplorer}.
	 */
	public static JpaExplorer doExplore(Collection<?> collection, String... patterns) {
		JpaExplorer jpaExplorer = new JpaExplorer();
		if (patterns != null) {
			for (String pattern : patterns) {
				GraphExplorer graphExplorer = GraphExplorer.get(pattern);
				graphExplorer.explore(collection, jpaExplorer);
			}
		}
		for (Object root : collection) {
			jpaExplorer.addJpaObject(root);
		}
		return jpaExplorer;
	}

}

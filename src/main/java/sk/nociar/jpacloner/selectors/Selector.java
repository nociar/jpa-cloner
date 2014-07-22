package sk.nociar.jpacloner.selectors;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Type-safe property selector based on metamodel classes needed for type-safe
 * Criteria queries as defined by JPA 2. 
 * See <a href='http://hibernate.org/orm/tooling/'>Hibernate Metamodel Generator</a>
 * for more details. <br/>
 * <b>NOTE:</b> this is an experimental feature
 * 
 * @author Miroslav Nociar
 *
 * @param <X>
 *            type of entity
 */
public class Selector<X> {
	
	protected final String path;
	protected final Set<String> paths;

	Selector(String path, Set<String> paths) {
		this.path = path;
		this.paths = paths;
	}

	public static <X> Selector<X> get(Class<X> root) {
		return new Selector<X>("", new HashSet<String>());
	}
	
	public final <Y> Selector<Y> join(SingularAttribute<? super X, Y> attribute) {
		return new Selector<Y>(createPath(attribute), paths);
	}
	
	public final <Y> Selector<Y> join(CollectionAttribute<? super X, Y> attribute) {
		return new Selector<Y>(createPath(attribute), paths);
	}
	
	public final <Y> Selector<Y> join(ListAttribute<? super X, Y> attribute) {
		return new Selector<Y>(createPath(attribute), paths);
	}
	
	public final <Y> Selector<Y> join(SetAttribute<? super X, Y> attribute) {
		return new Selector<Y>(createPath(attribute), paths);
	}
	
	public final <K, V> MapSelector<K, V> join(MapAttribute<? super X, K, V> attribute) {
		return new MapSelector<K, V>(createPath(attribute), paths);
	}
	
	private String createPath(Attribute<? super X, ?> attribute) {
		final String path = getPath();
		final String name = attribute.getName();
		final String next = path.isEmpty() ? name : path + "." + name;
		paths.add(next);
		paths.remove(path);
		paths.remove(this.path);
		return next;
	}
	
	protected String getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (String p : paths) {
			sb.append(delimiter).append(p);
			delimiter = "|";
		}
		return sb.toString();
	}
	
}

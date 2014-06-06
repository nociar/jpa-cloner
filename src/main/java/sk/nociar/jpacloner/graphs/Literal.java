package sk.nociar.jpacloner.graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Literal extends GraphExplorer {
	private final String literal;

	public Literal(String literal) {
		this.literal = literal;
	}

	@Override
	public Set<?> explore(Collection<?> entities, EntityExplorer entityExplorer) {
		Set<Object> explored = new HashSet<Object>();
		for (Object entity : entities) {
			Collection<?> value = entityExplorer.explore(entity, literal);
			if (value != null) {
				explored.addAll(value);
			}
		}
		return explored;
	}
}

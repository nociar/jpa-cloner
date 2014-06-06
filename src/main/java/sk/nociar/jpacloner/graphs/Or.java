package sk.nociar.jpacloner.graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class Or extends GraphExplorer {
	private final GraphExplorer a;
	private final GraphExplorer b;

	public Or(GraphExplorer a, GraphExplorer b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public Set<?> explore(Collection<?> entities, EntityExplorer entityExplorer) {
		Set<Object> explored = new HashSet<Object>();
		explored.addAll(a.explore(entities, entityExplorer));
		explored.addAll(b.explore(entities, entityExplorer));
		return explored;
	}
}
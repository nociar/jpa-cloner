package sk.nociar.jpacloner.graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class Multi extends GraphExplorer {
	private final GraphExplorer child;

	public Multi(GraphExplorer child) {
		this.child = child;
	}

	@Override
	public Set<?> explore(Collection<?> entities, EntityExplorer entityExplorer) {
		Set<Object> explored = new HashSet<Object>();
		Set<?> next = new HashSet<Object>(entities);
		do {
			next = child.explore(next, entityExplorer);
			// remove already explored entities (optimization & prevention of cycles)
			next.removeAll(explored);
			explored.addAll(next);
		} while (!next.isEmpty());

		return explored;
	}
}
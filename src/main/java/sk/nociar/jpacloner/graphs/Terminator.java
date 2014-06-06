package sk.nociar.jpacloner.graphs;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class Terminator extends GraphExplorer {
	private final GraphExplorer child;

	public Terminator(GraphExplorer child) {
		this.child = child;
	}

	@Override
	public Set<?> explore(Collection<?> entities, EntityExplorer entityExplorer) {
		// Explore the node an return an empty list.
		child.explore(entities, entityExplorer);
		return Collections.emptySet();
	}
}

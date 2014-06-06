package sk.nociar.jpacloner.graphs;

import java.util.Collection;
import java.util.Set;

public final class Dot extends GraphExplorer {
	private final GraphExplorer a;
	private final GraphExplorer b;

	public Dot(GraphExplorer a, GraphExplorer b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public Set<?> explore(Collection<?> entities, EntityExplorer entityExplorer) {
		return b.explore(a.explore(entities, entityExplorer), entityExplorer);
	}
}
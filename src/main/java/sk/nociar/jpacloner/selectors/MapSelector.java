package sk.nociar.jpacloner.selectors;

import java.util.Set;

public class MapSelector<K, V> extends Selector<V> {
	
	MapSelector(String path, Set<String> paths) {
		super(path, paths);
	}
	
	@Override
	protected String getPath() {
		return path + ".value";
	}
	
	public Selector<K> key() {
		return new Selector<K>(path + ".key", paths);
	}
	
	public Selector<V> value() {
		return new Selector<V>(path + ".value", paths);
	}

}

package sk.nociar.jpacloner.graphs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WildcardPattern extends GraphExplorer {
	private static final Pattern p = Pattern.compile("\\*|\\?|[^\\*\\?]+");
	private static final Map<String, WildcardPattern> cache = new HashMap<String, WildcardPattern>();
	
	private final Pattern pattern;
	
	private final ConcurrentMap<String, Boolean> isMatched = new ConcurrentHashMap<String, Boolean>();

	private WildcardPattern(String s) {
		StringBuilder sb = new StringBuilder("^");
		Matcher m = p.matcher(s);
		while (m.find()) {
			String token = m.group();
			if ("?".equals(token)) {
				sb.append(".");
			} else if ("*".equals(token)) {
				sb.append(".*?"); // Reluctant quantifier
			} else {
				sb.append(Pattern.quote(token));
			}
		}
		sb.append("$");
		pattern = Pattern.compile(sb.toString());
	}
	
	public static synchronized WildcardPattern get(String s) {
		WildcardPattern wildcardPattern = cache.get(s);
		if (wildcardPattern == null) {
			wildcardPattern = new WildcardPattern(s);
			cache.put(s, wildcardPattern);
		}
		return wildcardPattern;
	}

	@Override
	public Set<?> explore(Collection<?> entities, EntityExplorer entityExplorer) {
		Set<Object> explored = new HashSet<Object>();
		for (Object entity : entities) {
			for (String property : entityExplorer.getProperties(entity)) {
				Boolean matches = isMatched.get(property);
				if (matches == null) {
					matches = pattern.matcher(property).matches();
					isMatched.put(property, matches);
				}
				if (matches) {
					Collection<?> value = entityExplorer.explore(entity, property);
					if (value != null) {
						explored.addAll(value);
					}
				}
			}
		}
		return explored;
	}
}

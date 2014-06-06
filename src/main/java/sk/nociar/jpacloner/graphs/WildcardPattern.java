package sk.nociar.jpacloner.graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WildcardPattern extends GraphExplorer {
	private static final Pattern p = Pattern.compile("\\*|\\?|[^\\*\\?]+");
	
	private final Pattern pattern;

	public WildcardPattern(String s) {
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

	@Override
	public Set<?> explore(Collection<?> entities, EntityExplorer entityExplorer) {
		Set<Object> explored = new HashSet<Object>();
		for (Object entity : entities) {
			for (String property : entityExplorer.getProperties(entity)) {
				if (pattern.matcher(property).matches()) {
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

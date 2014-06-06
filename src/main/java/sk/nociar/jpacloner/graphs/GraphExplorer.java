/*   JPA cloner project.
 *   
 *   Copyright (C) 2013 Miroslav Nociar
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package sk.nociar.jpacloner.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic explorer of paths in a graph. It is required that the graph is stable
 * i.e. does not change during the exploring. The explorer will generate
 * property paths upon the pattern passed in the factory method, see
 * {@link GraphExplorer#get(String)}. The pattern supports following operators:
 * <ul>
 * <li>Dot "." separates paths.</li>
 * <li>Plus "+" generates at least one preceding path.</li>
 * <li>Split "|" divides the path into two ways.</li>
 * <li>Terminator "$" ends the preceding path.</li>
 * <li>Parentheses "(", ")" groups the paths.</li>
 * <li>Wildcards "*", "?" in property names.</li>
 * </ul>
 * Some examples follow:
 * <ul>
 * <li>device.*</li>
 * <li>device.(interfaces.type|driver.author)</li>
 * <li>company.department+.(boss|employees).address</li>
 * <li>*+</li>
 * </ul>
 * 
 * <b>NOTE</b>: Entities MUST correctly implement
 * {@link Object#equals(Object obj)} and {@link Object#hashCode()}.
 * 
 * @author Miroslav Nociar
 */
public abstract class GraphExplorer {

	private static final Pattern p;
	private static final Set<String> operators = new HashSet<String>();
	private static final Map<String, Integer> operatorToPriority = new HashMap<String, Integer>();
	private static final int LITERAL_PRIORITY = 10;

	static {
		// init operators
		operators.add("(");
		operators.add(")");
		operators.add(".");
		operators.add("|");
		operators.add("+");
		operators.add("$");
		// init operator priorities
		operatorToPriority.put("|", 1);
		operatorToPriority.put(".", 2);
		operatorToPriority.put("+", 3);
		operatorToPriority.put("$", 4);
		// init pattern
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (String op : operators) {
			sb.append("\\").append(op);
		}
		sb.append("]|[^\\s");
		for (String op : operators) {
			sb.append("\\").append(op);
		}
		sb.append("]+");
		p = Pattern.compile(sb.toString());
	}

	private static final Map<String, GraphExplorer> cache = new ConcurrentHashMap<String, GraphExplorer>();

	/**
	 * Factory method for complex {@link GraphExplorer}. Returned instance is
	 * thread safe i.e. can be used by multiple threads in parallel.
	 */
	public static GraphExplorer get(String pattern) {
		// check the cache first
		GraphExplorer explorer = cache.get(pattern);
		if (explorer != null) {
			return explorer;
		}
		Matcher m = p.matcher(pattern);
		// evaluate priority of each token
		List<String> tokens = new ArrayList<String>();
		List<Integer> priorities = new ArrayList<Integer>();
		int offset = 0;
		while (m.find()) {
			String token = m.group();
			if ("(".equals(token)) {
				offset += LITERAL_PRIORITY;
				continue;
			}
			if (")".equals(token)) {
				offset -= LITERAL_PRIORITY;
				continue;
			}
			// add to tokens
			tokens.add(token);
			// compute the priority
			Integer priority = operatorToPriority.get(token);
			if (priority == null) {
				// literal
				priorities.add(offset + LITERAL_PRIORITY);
			} else {
				// operator
				priorities.add(offset + priority);
			}
		}
		if (offset != 0) {
			throw new IllegalArgumentException("Wrong parentheses!");
		}

		explorer = getExplorer(tokens, priorities);
		// save in the cache
		cache.put(pattern, explorer);
		return explorer;
	}

	private static GraphExplorer getExplorer(List<String> tokens, List<Integer> priorities) {
		if (tokens.size() != priorities.size()) {
			throw new IllegalStateException("Tokens & priorities does not match!");
		}
		if (tokens.isEmpty()) {
			throw new IllegalStateException("No tokens!");
		}
		// find the leftmost operator with the lowest priority
		int idx = -1;
		int minPriority = Integer.MAX_VALUE;
		for (int i = 0; i < priorities.size(); i++) {
			int priority = priorities.get(i);
			if (priority < minPriority) {
				minPriority = priority;
				idx = i;
			}
		}
		String token = tokens.get(idx);
		if (!operators.contains(token)) {
			if (tokens.size() != 1) {
				throw new IllegalArgumentException("Missing operator near: " + token);
			}
			if (token.contains("*") || token.contains("?")) {
				return new WildcardPattern(token);
			}
			return new Literal(token);
		}

		if (".".equals(token) || "|".equals(token)) {
			List<String> ta = tokens.subList(0, idx);
			List<String> tb = tokens.subList(idx + 1, tokens.size());
			List<Integer> pa = priorities.subList(0, idx);
			List<Integer> pb = priorities.subList(idx + 1, priorities.size());
			GraphExplorer a = getExplorer(ta, pa);
			GraphExplorer b = getExplorer(tb, pb);
			if (".".equals(token)) {
				return new Dot(a, b);
			} else {
				return new Or(a, b);
			}
		}

		if ("+".equals(token) || "$".equals(token)) {
			if (idx != (tokens.size() - 1)) {
				throw new IllegalArgumentException("Postfix unary operator must be the last token!");
			}
			List<String> ta = tokens.subList(0, idx);
			List<Integer> pa = priorities.subList(0, idx);
			GraphExplorer a = getExplorer(ta, pa);
			if ("$".equals(token)) {
				return new Terminator(a);
			} else {
				return new Multi(a);
			}
		}

		throw new IllegalStateException("Unknown tokens: " + tokens);
	}

	public abstract Set<?> explore(Collection<?> entities, EntityExplorer explorer);

}
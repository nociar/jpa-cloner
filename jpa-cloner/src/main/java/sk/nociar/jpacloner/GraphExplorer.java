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
package sk.nociar.jpacloner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Generic explorer of paths in a graph (thread safe implementation). It is
 * required that the graph is stable i.e. does not change during the exploring.
 * The explorer will generate property paths upon the pattern passed in the
 * constructor, see {@link GraphExplorer#GraphExplorer(String)}. The pattern
 * supports following operators:
 * <ul>
 * <li>Dot "." separates paths.</li>
 * <li>Star "*" generates any number of preceding path (including zero).</li>
 * <li>Plus "+" generates at least one preceding path.</li>
 * <li>Split "|" divides the path into two ways.</li>
 * <li>Terminator "$" ends the preceding path.</li>
 * <li>Parentheses "(", ")" groups the paths.</li>
 * </ul>
 * Some examples follow:
 * <ul>
 * <li>project.devices.interfaces</li>
 * <li>school.teachers.lessonToPupils.(key.class|value.parents)</li>
 * <li>(children.child)*.(nodeType|attributes.attributeType)</li>
 * <li>comapany.department*.(boss|employees).address.(country|city|street)</li>
 * </ul>
 * 
 * <b>NOTE</b>: Entities MUST correctly implement the
 * {@link Object#equals(Object obj)} method and the {@link Object#hashCode()}
 * method!
 * 
 * @author Miroslav Nociar
 */
public final class GraphExplorer {

	private interface Explorer {
		public Collection<Object> explore(Object entity, EntityExplorer nodeExplorer);
	}

	private final class Literal implements Explorer {
		private final String property;

		Literal(String property) {
			this.property = property;
		}

		@Override
		public Collection<Object> explore(Object entity, EntityExplorer nodeExplorer) {
			Collection<Object> value = nodeExplorer.explore(entity, property);
			return value == null ? Collections.emptySet() : value;
		}
	}

	private final class Dot implements Explorer {
		private final Explorer a;
		private final Explorer b;

		Dot(Explorer a, Explorer b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public Collection<Object> explore(Object entity, EntityExplorer nodeExplorer) {
			Set<Object> exploredNodes = new HashSet<Object>();
			for (Object o : a.explore(entity, nodeExplorer)) {
				exploredNodes.addAll(b.explore(o, nodeExplorer));
			}
			return exploredNodes;
		}
	}

	private final class Or implements Explorer {
		private final Explorer a;
		private final Explorer b;

		Or(Explorer a, Explorer b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public Collection<Object> explore(Object entity, EntityExplorer nodeExplorer) {
			Set<Object> exploredNodes = new HashSet<Object>(a.explore(entity, nodeExplorer));
			exploredNodes.addAll(b.explore(entity, nodeExplorer));
			return exploredNodes;
		}
	}

	private final class Multi implements Explorer {
		private final Explorer child;
		private final String operation;

		Multi(Explorer child, String operation) {
			this.child = child;
			this.operation = operation;
		}

		@Override
		public Collection<Object> explore(Object entity, EntityExplorer nodeExplorer) {
			Set<Object> explored = new HashSet<Object>();
			if ("*".equals(operation)) {
				// operator * returns at least the passed entity
				explored.add(entity);
			}

			Set<Object> next = new HashSet<Object>();
			next.add(entity);
			do {
				Set<Object> current = next;
				next = new HashSet<Object>();
				for (Object o : current) {
					next.addAll(child.explore(o, nodeExplorer));
				}
				// remove all explored nodes (optimization & prevention of cycles)
				next.removeAll(explored);
				explored.addAll(next);
			} while (!next.isEmpty());

			return explored;
		}
	}
	
	private final class Terminator implements Explorer {
		private final Explorer child;

		Terminator(Explorer child) {
			this.child = child;
		}

		@Override
		public Collection<Object> explore(Object entity, EntityExplorer nodeExplorer) {
			// Explore the node an return the empty list.
			child.explore(entity, nodeExplorer);
			return Collections.emptyList();
		}
	}

	private final Explorer explorer;

	private static final Pattern p;
	private static final Set<String> operators;
	private static final Map<String, Integer> operatorToPriority;
	private static final int LITERAL_PRIORITY = 10;

	static {
		// init pattern
		p = Pattern.compile("\\w+|\\.|\\||\\(|\\)|\\*|\\+|\\$");
		// init operators
		Set<String> set = new HashSet<String>();
		set.add("(");
		set.add(")");
		set.add(".");
		set.add("|");
		set.add("*");
		set.add("+");
		set.add("$");
		operators = Collections.unmodifiableSet(set);
		// init operator priorities
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("|", 1);
		map.put(".", 2);
		map.put("*", 3);
		map.put("+", 3);
		map.put("$", 4);
		operatorToPriority = Collections.unmodifiableMap(map);
	}

	public GraphExplorer(String pattern) {
		explorer = getExplorer(pattern);
	}

	private Explorer getExplorer(String pattern) {
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

		return getExplorer(tokens, priorities);
	}

	private Explorer getExplorer(List<String> tokens, List<Integer> priorities) {
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
			return new Literal(token);
		}

		if (".".equals(token) || "|".equals(token)) {
			List<String> ta = tokens.subList(0, idx);
			List<String> tb = tokens.subList(idx + 1, tokens.size());
			List<Integer> pa = priorities.subList(0, idx);
			List<Integer> pb = priorities.subList(idx + 1, priorities.size());
			Explorer a = getExplorer(ta, pa);
			Explorer b = getExplorer(tb, pb);
			if (".".equals(token)) {
				return new Dot(a, b);
			} else {
				return new Or(a, b);
			}
		}

		if ("*".equals(token) || "+".equals(token) || "$".equals(token)) {
			if (idx != (tokens.size() - 1)) {
				throw new IllegalArgumentException("Postfix unary operator must be the last token!");
			}
			List<String> ta = tokens.subList(0, idx);
			List<Integer> pa = priorities.subList(0, idx);
			Explorer a = getExplorer(ta, pa);
			if ("$".equals(token)) {
				return new Terminator(a);
			} else {
				return new Multi(a, token);
			}
		}

		throw new IllegalStateException("Unknown tokens: " + tokens);
	}

	public void explore(Object root, EntityExplorer nodeExplorer) {
		explorer.explore(root, nodeExplorer);
	}

}

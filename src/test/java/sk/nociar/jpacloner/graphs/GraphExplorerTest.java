package sk.nociar.jpacloner.graphs;

import org.junit.Test;

import sk.nociar.jpacloner.graphs.GraphExplorer;

public class GraphExplorerTest {

	@Test
	public void testNoException() {
		GraphExplorer.get("children.*");
		GraphExplorer.get("chil*n");
	}

	@Test(expected = RuntimeException.class)
	public void testException1() {
		GraphExplorer.get("(children");
	}

	@Test(expected = RuntimeException.class)
	public void testException2() {
		GraphExplorer.get("children)");
	}
	
	@Test(expected = RuntimeException.class)
	public void testException3() {
		GraphExplorer.get(".children");
	}

	@Test(expected = RuntimeException.class)
	public void testException4() {
		GraphExplorer.get("children.");
	}

	@Test(expected = RuntimeException.class)
	public void testException5() {
		GraphExplorer.get("$");
	}
	
	@Test(expected = RuntimeException.class)
	public void testException6() {
		GraphExplorer.get("(aaa)*");
	}

}

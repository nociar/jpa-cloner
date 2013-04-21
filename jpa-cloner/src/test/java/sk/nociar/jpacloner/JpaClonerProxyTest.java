package sk.nociar.jpacloner;

import org.junit.Before;
import org.junit.Test;

import sk.nociar.jpacloner.entities.Bar;
import sk.nociar.jpacloner.entities.Baz;
import sk.nociar.jpacloner.entities.Edge;
import sk.nociar.jpacloner.entities.Foo;
import sk.nociar.jpacloner.entities.Node;
import sk.nociar.jpacloner.entities.Point;

public class JpaClonerProxyTest {
	
	private static int idGenerator = 0;
	
	private static class EdgeProxy extends Edge {
		EdgeProxy() {
			setId(idGenerator++);
		}
	}
	
	private static class NodeProxy extends Node {
		NodeProxy() {
			setId(idGenerator++);
		}
	}
	
	private static class FooProxy extends Foo {
		FooProxy() {
			setId(idGenerator++);
		}
	}
	
	private static class BarProxy extends Bar {
		BarProxy() {
			setId(idGenerator++);
		}
	}

	private static class BazProxy extends Baz {
		BazProxy() {
			setId(idGenerator++);
		}
	}

	private static class PointProxy extends Point {
	}
	
	private JpaClonerTestSupport support;
	
	@Before
	public void setUp() {
		support = new JpaClonerTestSupport() {
			@Override
			protected Node createNode() {
				return new NodeProxy();
			}
			@Override
			protected Edge createEdge() {
				return new EdgeProxy();
			}
			@Override
			protected Point createPoint() {
				return new PointProxy();
			}
			@Override
			protected Foo createFoo() {
				return new FooProxy();
			}
			@Override
			protected Bar createBar() {
				return new BarProxy();
			}
			@Override
			protected Baz createBaz() {
				return new BazProxy();
			}
		};
		
		support.initialize();
	}

	@Test
	public void testClone() {
		support.testClone1();
	}

	@Test
	public void testClone2() {
		support.testClone2();
	}

	@Test
	public void testClone3() {
		support.testClone3();
	}

	@Test
	public void testClone4() {
		support.testClone4();
	}


	public void testNoException() {
		JpaCloner.clone(new NodeProxy(), "bar", "xxx", "(yyy)");
	}

	@Test(expected = RuntimeException.class)
	public void testException1() {
		new GraphExplorer("(children");
	}

	@Test(expected = RuntimeException.class)
	public void testException2() {
		new GraphExplorer("children)");
	}
	
	@Test(expected = RuntimeException.class)
	public void testException3() {
		new GraphExplorer("*children");
	}

	@Test(expected = RuntimeException.class)
	public void testException4() {
		new GraphExplorer(".children");
	}

	@Test(expected = RuntimeException.class)
	public void testException5() {
		new GraphExplorer("children.");
	}

	@Test(expected = RuntimeException.class)
	public void testException6() {
		new GraphExplorer("$");
	}
	
}

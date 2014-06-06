package sk.nociar.jpacloner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sk.nociar.jpacloner.entities.Bar;
import sk.nociar.jpacloner.entities.Baz;
import sk.nociar.jpacloner.entities.Edge;
import sk.nociar.jpacloner.entities.Foo;
import sk.nociar.jpacloner.entities.Node;
import sk.nociar.jpacloner.entities.Point;
import sk.nociar.jpacloner.entities.DummyEntity;
import sk.nociar.jpacloner.graphs.GraphExplorer;
import sk.nociar.jpacloner.graphs.PropertyFilter;

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
				return new Point(1, 2);
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

	@Test
	public void testClone5() {
		support.testClone5();
	}

	@Test
	public void testExplore() {
		support.testExplore();
	}
	
	@Test
	public void testNullClone() {
		Assert.assertNull(JpaCloner.clone((Object)null, "foo.bar"));
		JpaCloner jpaCloner = new JpaCloner();
		Assert.assertNull(jpaCloner.getClone(null));
	}
	
	@Test
	public void testNoGetter() {
		DummyEntity dummy = new DummyEntity();
		dummy.setId(123);
		dummy.i = 666;
		dummy.s = "hello world";
		DummyEntity clone = JpaCloner.clone(dummy);
		Assert.assertNotSame(dummy, clone);
		Assert.assertEquals(dummy, clone);
		Assert.assertEquals(dummy.i, clone.i);
		Assert.assertEquals(dummy.s, clone.s);
		
		clone = JpaCloner.clone(dummy, new PropertyFilter() {
			@Override
			public boolean test(Object entity, String property) {
				return !"i".equals(property);
			}
		});
		Assert.assertEquals(0, clone.i);
		Assert.assertEquals("hello world", clone.s);
	}

	@Test
	public void testNoException() {
		JpaCloner.clone(new NodeProxy(), "bar", "xxx", "(yyy)");
		GraphExplorer.get("children.*");
		GraphExplorer.get("chil*n");
		;
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

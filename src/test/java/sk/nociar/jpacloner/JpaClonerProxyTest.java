package sk.nociar.jpacloner;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Transient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sk.nociar.jpacloner.entities.A;
import sk.nociar.jpacloner.entities.B;
import sk.nociar.jpacloner.entities.Bar;
import sk.nociar.jpacloner.entities.Baz;
import sk.nociar.jpacloner.entities.C;
import sk.nociar.jpacloner.entities.DummyEntity;
import sk.nociar.jpacloner.entities.Edge;
import sk.nociar.jpacloner.entities.Foo;
import sk.nociar.jpacloner.entities.Node;
import sk.nociar.jpacloner.entities.Point;
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
	public void testClone1() {
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
	}
	
	@Test
	public void testCloningOrder() {
		A a = new A();
		B b1 = new B();
		B b2 = new B();
		B b3 = new B();
		C c = new C();
		// IDs
		a.setId(1);
		b1.setId(1);
		b2.setId(2);
		b3.setId(3);
		c.setId(1);
		// @ManyToOne
		b1.setA(a);
		b2.setA(a);
		b3.setA(a);
		b1.setC(c);
		b2.setC(c);
		b3.setC(c);
		// @OneToMany
		Set<B> set = new HashSet<B>();
		set.add(b1);
		set.add(b2);
		set.add(b3);
		a.setSet(set);
		
		// clone (ignore transient fields)
		A a_clone = JpaCloner.clone(a, PropertyFilters.getAnnotationFilter(Transient.class), "set.c");
		// verify set of B object
		Assert.assertEquals(a_clone.getSet().size(), 3);
		for (B b : a_clone.getSet()) {
			Assert.assertTrue(b.counter_a < b.counter_hashcode);
			Assert.assertTrue(b.counter_c < b.counter_hashcode);
		}
	}
}

package sk.nociar.jpacloner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import sk.nociar.jpacloner.entities.Bar;
import sk.nociar.jpacloner.entities.BaseEntity;
import sk.nociar.jpacloner.entities.Baz;
import sk.nociar.jpacloner.entities.Edge;
import sk.nociar.jpacloner.entities.Foo;
import sk.nociar.jpacloner.entities.Node;
import sk.nociar.jpacloner.entities.Point;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, 
	classes = { AppConfig.class, DataConfig.class })
public class JpaClonerTest {
	@PersistenceContext
	EntityManager em;

	private void persist(List<? extends BaseEntity> list) {
		for (BaseEntity e : list) {
			em.persist(e);
		}
		em.flush();
	}
	
	private JpaClonerTestSupport support;

	private List<Node> nodeList;
	private List<Edge> edgeList;
	private List<Foo> fooList;
	private List<Baz> bazList;
	private List<Bar> barList;
	
	@Before
	@Transactional
	public void setUp() {
		nodeList = new ArrayList<Node>();
		edgeList = new ArrayList<Edge>();
		fooList = new ArrayList<Foo>();
		bazList = new ArrayList<Baz>();
		barList = new ArrayList<Bar>();
		
		support = new JpaClonerTestSupport() {
			@Override
			protected Node createNode() {
				Node entity = new Node();
				nodeList.add(entity);
				return entity;
			}
			@Override
			protected Edge createEdge() {
				Edge entity = new Edge();
				edgeList.add(entity);
				return entity;
			}
			@Override
			protected Point createPoint() {
				return new Point();
			}
			@Override
			protected Foo createFoo() {
				Foo entity = new Foo();
				fooList.add(entity);
				return entity;
			}
			@Override
			protected Bar createBar() {
				Bar entity = new Bar();
				barList.add(entity);
				return entity;
			}
			@Override
			protected Baz createBaz() {
				Baz entity = new Baz();
				bazList.add(entity);
				return entity;
			}
			@Override
			protected Node getOriginal() {
				TypedQuery<Node> q = em.createQuery("select n from Node n where n.name='1'", Node.class);
				return q.getSingleResult();
			}
		};
		
		support.initialize();
		
		persist(barList);
		persist(bazList);
		persist(fooList);
		persist(nodeList);
		persist(edgeList);
		// detach all entities from persistence ctx
		em.clear();
	}
	
	@Test
	@Transactional
	public void testClone() {
		support.testClone1();
	}

	@Test
	@Transactional
	public void testClone2() {
		support.testClone2();
	}

	@Test
	@Transactional
	public void testClone3() {
		support.testClone3();
	}

	@Test
	@Transactional
	public void testClone4() {
		support.testClone4();
	}
	
	@Test
	@Transactional
	public void testCloneList() {
		TypedQuery<Node> q = em.createQuery("select n from Node n", Node.class);
		List<Node> nodes = JpaCloner.clone(q.getResultList(), "point", "(foo|baz).bar");
		// assert that the same objects are correctly cloned/references
		Map<Object, Object> instances = new HashMap<Object, Object>();
		for (Node node : nodes) {
			if (node.getBaz() != null) {
				assertSingleInstance(node.getBaz(), instances);
				assertSingleInstance(node.getBaz().getBar(), instances);
			}
			if (node.getFoo() != null) {
				assertSingleInstance(node.getFoo(), instances);
				assertSingleInstance(node.getFoo().getBar(), instances);
			}
		}
	}
	
	private void assertSingleInstance(Object object, Map<Object, Object> instances) {
		Object instance = instances.get(object);
		if (instance == null) {
			instances.put(object, object);
		} else {
			Assert.assertSame(instance, object);
		}
	}
}

package sk.nociar.jpacloner.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;


@Entity
public class Node extends BaseEntity {
	private String name;
	
	@OneToMany(mappedBy = "parent")
	@MapKey(name = "position")
	private Map<Integer, Edge> children = new HashMap<Integer, Edge>();
	
	@OneToMany(mappedBy = "child")
	private Set<Edge> parents = new HashSet<Edge>();
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="foo_id")
	private Foo foo;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="baz_id")
	private Baz baz;
	
	@Embedded
	private Point point;

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Integer, Edge> getChildren() {
		return children;
	}

	public void setChildren(Map<Integer, Edge> children) {
		this.children = children;
	}

	public Set<Edge> getParents() {
		return parents;
	}

	public void setParents(Set<Edge> parents) {
		this.parents = parents;
	}

	public Foo getFoo() {
		return foo;
	}

	public void setFoo(Foo foo) {
		this.foo = foo;
	}

	public Baz getBaz() {
		return baz;
	}

	public void setBaz(Baz baz) {
		this.baz = baz;
	}

}

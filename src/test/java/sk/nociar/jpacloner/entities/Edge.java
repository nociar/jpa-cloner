package sk.nociar.jpacloner.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class Edge extends BaseEntity {
	@ManyToOne
	@JoinColumn(name="parent_id")
	private Node parent;
	
	@ManyToOne
	@JoinColumn(name="child_id")
	private Node child;

	@ManyToOne
	@JoinColumn(name="bar_id")
	private Bar bar;

	@Column(name="pos")
	private int position;
	
	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getChild() {
		return child;
	}

	public void setChild(Node child) {
		this.child = child;
	}

	public Bar getBar() {
		return bar;
	}

	public void setBar(Bar bar) {
		this.bar = bar;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}

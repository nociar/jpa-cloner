package sk.nociar.jpacloner.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class Baz extends BaseEntity {
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="bar_id")
	private Bar bar;

	public Bar getBar() {
		return bar;
	}

	public void setBar(Bar bar) {
		this.bar = bar;
	}

}

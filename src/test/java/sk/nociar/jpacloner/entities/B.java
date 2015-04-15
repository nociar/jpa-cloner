package sk.nociar.jpacloner.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class B {
	@Transient
	private int counter = 1;
	@Transient
	public int counter_a = 0;
	@Transient
	public int counter_c = 0;
	@Transient
	public int counter_hashcode = 0;
	
	@Id
	private Integer id;
	
	@ManyToOne
	private A a;
	
	@ManyToOne
	private C c;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
		if (counter_a == 0) {
			counter_a = counter++;
		}
	}

	public C getC() {
		return c;
	}

	public void setC(C c) {
		this.c = c;
		if (counter_c == 0) {
			counter_c = counter++;
		}
	}
	
	@Override
	public int hashCode() {
		if (counter_hashcode == 0) {
			counter_hashcode = counter++;
		}
		return super.hashCode();
	}
}

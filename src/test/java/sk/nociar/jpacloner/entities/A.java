package sk.nociar.jpacloner.entities;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class A {
	@Id
	private Integer id;
	
	@OneToMany(mappedBy="a")
	private Set<B> set;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Set<B> getSet() {
		return set;
	}

	public void setSet(Set<B> set) {
		this.set = set;
	}
}

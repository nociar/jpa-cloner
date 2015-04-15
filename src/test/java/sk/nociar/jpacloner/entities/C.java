package sk.nociar.jpacloner.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class C {
	@Id
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}

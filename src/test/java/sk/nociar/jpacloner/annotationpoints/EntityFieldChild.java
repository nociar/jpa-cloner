package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EntityFieldChild {

	@Id
	private Integer id;

	private Integer otherField;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Integer getOtherField() {
		return otherField;
	}

	public void setOtherField(final Integer otherField) {
		this.otherField = otherField;
	}
}

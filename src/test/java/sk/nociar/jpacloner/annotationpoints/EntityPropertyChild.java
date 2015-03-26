package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EntityPropertyChild {

	private Integer id;

	private Integer otherField;

	@Id
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

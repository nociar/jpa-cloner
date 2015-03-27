package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;

@Entity
@Access(AccessType.PROPERTY)
public class AccessProperty {

	private Integer field;

	public Integer getField() {
		return field;
	}

	public void setField(final Integer field) {
		this.field = field * 2;
	}
}

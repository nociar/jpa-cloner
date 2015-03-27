package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Access(AccessType.FIELD)
public class AccessField {

	private Integer field;

	public Integer getField() {
		return field;
	}

	public void setField(final Integer field) {
		this.field = field * 2;
	}
}

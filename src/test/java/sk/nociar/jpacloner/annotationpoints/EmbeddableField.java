package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Embeddable
@Access(AccessType.FIELD)
public class EmbeddableField {

	private Integer field;

	public Integer getField() {
		return field;
	}

	public void setField(final Integer field) {
		this.field = field == null ? null : field * 2;
	}
}

package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Entity;

@Entity
public class EntityNoId {

	private Integer id;

	private Integer otherField;

	public Integer differentNameField;

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

	public Integer getDifferentNameField2() {
		return differentNameField;
	}

	public void setDifferentNameField2(final Integer differentNameField2) {
		this.differentNameField = differentNameField2;
	}
}

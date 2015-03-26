package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class EntityField {

	@Id
	private Integer id;

	private Integer otherField;

	public Integer noSetterField;

	public Integer differentNameField;

	@OneToOne
	private EntityFieldChild child;

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

	public EntityFieldChild getChild() {
		return child;
	}

	public void setChild(final EntityFieldChild child) {
		this.child = child;
	}

	public Integer getNoSetterField() {
		return noSetterField;
	}

	public Integer getDifferentNameField2() {
		return differentNameField;
	}

	public void setDifferentNameField2(final Integer differentNameField2) {
		this.differentNameField = differentNameField2;
	}
}

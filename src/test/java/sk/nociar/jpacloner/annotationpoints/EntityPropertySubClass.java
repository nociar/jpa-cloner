package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Entity;

@Entity
public class EntityPropertySubClass extends EntityProperty {

	private Integer newField;

	public Integer getNewField() {
		return newField;
	}

	public void setNewField(final Integer newField) {
		this.newField = newField;
	}
}

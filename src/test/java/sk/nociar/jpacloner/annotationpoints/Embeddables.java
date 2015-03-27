package sk.nociar.jpacloner.annotationpoints;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@Access(AccessType.FIELD)
public class Embeddables {

	@Embedded
	private EmbeddableField field = new EmbeddableField();

	@Embedded
	private EmbeddableProperty property = new EmbeddableProperty();

	public EmbeddableField getField() {
		return field;
	}

	public void setField(final EmbeddableField field) {
		this.field = field;
	}

	public EmbeddableProperty getProperty() {
		return property;
	}

	public void setProperty(final EmbeddableProperty property) {
		this.property = property;
	}
}

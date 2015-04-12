package sk.nociar.jpacloner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import sk.nociar.jpacloner.properties.PropertyReader;
import sk.nociar.jpacloner.properties.PropertyWriter;

/**
 * Info about JPA property (basic field or relation).
 */
public class JpaPropertyInfo {
	private final AccessibleObject accessibleObject;
	private final PropertyReader propertyReader;
	private final PropertyWriter propertyWriter;
	private final List<String> mappedBy;
	private final boolean isBasic;
	private final boolean isSingular;
	
	public JpaPropertyInfo(AccessibleObject accessibleObject, PropertyReader propertyReader, PropertyWriter propertyWriter) {
		this.accessibleObject = accessibleObject;
		this.propertyReader = propertyReader;
		this.propertyWriter = propertyWriter;
		
		final ManyToOne manyToOne = accessibleObject.getAnnotation(ManyToOne.class);
		final OneToOne oneToOne = accessibleObject.getAnnotation(OneToOne.class);
		final OneToMany oneToMany = accessibleObject.getAnnotation(OneToMany.class);
		final ManyToMany manyToMany = accessibleObject.getAnnotation(ManyToMany.class);
		final Embedded embedded = accessibleObject.getAnnotation(Embedded.class);
		final EmbeddedId embeddedId = accessibleObject.getAnnotation(EmbeddedId.class);
		final ElementCollection elementCollection = accessibleObject.getAnnotation(ElementCollection.class);

		if (allNull(manyToOne, oneToOne, oneToMany, manyToMany, embedded, embeddedId, elementCollection)) {
			// basic field
			isBasic = true;
			isSingular = true;
			mappedBy = null;
		} else {
			// relation/embedded field
			isBasic = false;
			isSingular = allNull(oneToMany, manyToMany, elementCollection);
			// handle mappedBy for @OneToOne or @OneToMany
			// NOTE handling of mappedBy for @ManyToMany is omitted intentionally
			String mappedName = null;
			if (oneToOne != null) {
				mappedName = oneToOne.mappedBy();
			} else if (oneToMany != null) {
				mappedName = oneToMany.mappedBy();
			}
			if (mappedName != null && !mappedName.trim().isEmpty()) {
				mappedName = mappedName.trim();
				// NOTE: the mappedBy attribute may be used in @Embeddable
				if (mappedName.contains(".")) {
					mappedBy = unmodifiableList(asList(mappedName.split("\\.")));
				} else {
					mappedBy = singletonList(mappedName);
				}
			} else {
				mappedBy = null;
			}
		}		
	}
	
	private boolean allNull(Annotation... annotations) {
		for (Annotation a : annotations) {
			if (a != null) {
				return false;
			}
		}
		return true;
	}

	public AccessibleObject getAccessibleObject() {
		return accessibleObject;
	}
	
	public Object getValue(Object instance) {
		return propertyReader.get(instance);
	}
	
	public void setValue(Object instance, Object value) {
		propertyWriter.set(instance, value);
	}

	public List<String> getMappedBy() {
		return mappedBy;
	}

	public boolean isBasic() {
		return isBasic;
	}
	
	public boolean isSingular() {
		return isSingular;
	}
}

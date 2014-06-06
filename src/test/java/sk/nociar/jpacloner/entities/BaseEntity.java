package sk.nociar.jpacloner.entities;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import sk.nociar.jpacloner.AbstractJpaExplorer;

@MappedSuperclass
public class BaseEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public final boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		// get JPA classes (non-proxy) 
		Class<?> thisClass = AbstractJpaExplorer.getJpaClass(this.getClass());
		Class<?> otherClass = AbstractJpaExplorer.getJpaClass(other.getClass());

		if (thisClass != otherClass) {
			return false;
		}
		Integer thisId = getId();
		Integer otherId = ((BaseEntity) other).getId();

		if (thisId == null || otherId == null) {
			return false;
		}

		return thisId.equals(otherId);
	}

	@Override
	public final int hashCode() {
		Integer thisId = getId();
		return thisId == null ? super.hashCode() : thisId.hashCode();
	}

}

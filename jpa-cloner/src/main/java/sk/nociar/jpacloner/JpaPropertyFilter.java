package sk.nociar.jpacloner;

public interface JpaPropertyFilter {
	/**
	 * Returns <code>true</code> if a property of an entity should be cloned,
	 * <code>false</code> otherwise.
	 * 
	 * @param entity
	 *            the JPA entity
	 * @param property
	 *            the basic JPA property
	 */
	public boolean isCloned(Object entity, String property);
}

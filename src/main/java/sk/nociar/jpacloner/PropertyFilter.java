package sk.nociar.jpacloner;

/**
 * Simple property filter. <br/>
 * <b>NOTE</b> this interface could be replaced by 
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/function/BiPredicate.html">BiPredicate</a>
 * after the Java 8 will be adopted.
 * 
 * @author Miroslav Nociar
 */
public interface PropertyFilter {
	/**
	 * Returns <code>true</code> if a property of an entity should be processed,
	 * <code>false</code> otherwise.
	 * 
	 * @param entity
	 *            the entity
	 * @param property
	 *            the property
	 */
	public boolean test(Object entity, String property);
}

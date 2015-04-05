package sk.nociar.jpacloner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.nociar.jpacloner.graphs.PropertyFilter;

/**
 * Factory of various {@link PropertyFilter}s. Example:<br/>
 * <pre>
 * PropertyFilter filter = PropertyFilterFactory.getAnnotationFilter(Id.class, Transient.class, Version.class); 
 * Company cloned = JpaCloner.clone(company, filter, "department+.(boss|employees).address");
 * </pre>
 * 
 * @author Miroslav Nociar
 */
public class PropertyFilters {
	
	private PropertyFilters() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Default property filter.
	 */
	private static final PropertyFilter defaultFilter = new PropertyFilter() {
		@Override
		public boolean test(Object entity, String property) {
			return true;
		}
	};
	
	private static final class AnnotationFilter implements PropertyFilter {
		private final Class<? extends Annotation> clazz;

		private AnnotationFilter(Class<? extends Annotation> clazz) {
			if (clazz == null) {
				throw new NullPointerException();
			}
			this.clazz = clazz;
		}

		@Override
		public boolean test(Object entity, String property) {
			JpaClassInfo classInfo = AbstractJpaExplorer.getClassInfo(entity);
			if (classInfo == null) {
				return true;
			}
			JpaPropertyInfo propertyInfo = classInfo.getPropertyInfo(property);
			if (propertyInfo == null) {
				return true;
			}
			return propertyInfo.getAccessibleObject().getAnnotation(clazz) == null;
		}
	}
	
	private static final class ComposedFilter implements PropertyFilter {
		
		private final List<PropertyFilter> filters;

		private ComposedFilter(List<PropertyFilter> filters) {
			this.filters = filters;
		}
		
		@Override
		public boolean test(Object entity, String property) {
			for (PropertyFilter filter : filters) {
				if (filter.test(entity, property) != true) {
					// filter did not pass
					return false;
				}
			}
			return true;
		}
	}

	public static PropertyFilter getDefaultFilter() {
		return defaultFilter;
	}
	
	public static PropertyFilter getComposedFilter(PropertyFilter filter, PropertyFilter... filters) {
		List<PropertyFilter> all = new ArrayList<PropertyFilter>();
		all.add(filter);
		if (filters != null) {
			all.addAll(Arrays.asList(filters));
		}
		return new ComposedFilter(all);
	}
	
	public static PropertyFilter getAnnotationFilter(Class<? extends Annotation> clazz) {
		return new AnnotationFilter(clazz);
	}
	
	public static PropertyFilter getAnnotationFilter(Class<? extends Annotation> c1, Class<? extends Annotation> c2) {
		return getComposedFilter(getAnnotationFilter(c1), getAnnotationFilter(c2));
	}
	
	public static PropertyFilter getAnnotationFilter(Class<? extends Annotation> c1, Class<? extends Annotation> c2, Class<? extends Annotation> c3) {
		return getComposedFilter(getAnnotationFilter(c1), getAnnotationFilter(c2), getAnnotationFilter(c3));
	}
	
	public static PropertyFilter getAnnotationFilter(Class<? extends Annotation> c1, Class<? extends Annotation> c2, Class<? extends Annotation> c3, Class<? extends Annotation> c4) {
		return getComposedFilter(getAnnotationFilter(c1), getAnnotationFilter(c2), getAnnotationFilter(c3), getAnnotationFilter(c4));
	}

	public static PropertyFilter getAnnotationFilter(Class<? extends Annotation> c1, Class<? extends Annotation> c2, Class<? extends Annotation> c3, Class<? extends Annotation> c4, Class<? extends Annotation> c5) {
		return getComposedFilter(getAnnotationFilter(c1), getAnnotationFilter(c2), getAnnotationFilter(c3), getAnnotationFilter(c4), getAnnotationFilter(c5));
	}
}

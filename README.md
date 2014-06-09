
# JPA cloner #

The project allows cloning of JPA entity _**subgraphs**_. Entity subgraphs are defined by string patterns.
String patterns define **included relations** which will be cloned.
Cloned entities will have all **basic properties** (non-relation properties) copied by default.
Advanced control over the cloning process is supported via the _**PropertyFilter**_ interface.
```xml
<dependency>
    <groupId>com.github.nociar</groupId>
    <artifactId>jpa-cloner</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Example usage
```java
Company company = entityManager.find(Company.class, companyId);
Company clone1 = JpaCloner.clone(company, "partners.*");
Company clone2 = JpaCloner.clone(company, "depa??ment");
Company clone3 = JpaCloner.clone(company, "department+.(boss|employees).address");
// do not clone Id and Transient fields for the whole entity subgraph:
PropertyFilter filter = PropertyFilters.getAnnotationFilter(Id.class, Transient.class);
Company clone4 = JpaCloner.clone(company, filter, "*+");
```

## Operators
- Dot "." separates paths.
- Plus "+" generates at least one preceding path.
- Split "|" divides the path into two ways.
- Terminator "$" ends the preceding path.
- Parentheses "(", ")" groups the paths.
- Wildcards "*", "?" in property names.

##Requirements
- The JPA cloner is tested only against **Hibernate**.
- Cloned entities must use JPA annotations and the _**field access**_, NOT the property access.
- Cloned entities must _**correctly**_ implement equals() and hashCode().

Please refer to the _**JpaCloner**_ class for more description.
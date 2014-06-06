
# JPA cloner #

The project allows cloning of JPA entity _**subgraphs**_. Entity subgraphs are defined by string patterns.
String patterns define **included relations** which will be cloned.
Advanced control over the cloning process is supported via the _**PropertyFilter**_ interface.
```xml
<dependency>
    <groupId>com.github.nociar</groupId>
    <artifactId>jpa-cloner</artifactId>
    <version>1.0.0</version>
</dependency>
```
## Example usage
```java
Company company = entityManager.find(Company.class, companyId);
Company clone1 = JpaCloner.clone(company, "depa??ment");
Company clone2 = JpaCloner.clone(company, "partners.*");
Company clone3 = JpaCloner.clone(company, "address", "department+.(boss|employees).address");
```
```java
Company company = entityManager.find(Company.class, companyId);
// do not clone primary keys for the whole entity subgraph
PropertyFilter filter = PropertyFilterFactory.getAnnotationFilter(Id.class, Transient.class);
Company clone = JpaCloner.clone(company, filter, "*+");
```
##Requirements
- The JPA cloner is tested only against **Hibernate**.
- Cloned entities must use JPA annotations and the _**field access**_, NOT the property access.
- Cloned entities must _**correctly**_ implement equals() and hashCode().

Please refer to the _**JpaCloner**_ class for more description.

# JPA cloner #

The project allows cloning of JPA entity _**subgraphs**_. Entity subgraphs are defined by string patterns or by a custom _PropertyFilter_. The string patterns support operators to allow various expressions of property paths.

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
Company clone1 = JpaCloner.clone(company);
Company clone2 = JpaCloner.clone(company, "department*.employees");
Company clone3 = JpaCloner.clone(company, "address", "department+.(boss|employees).address);
```

```java
Company company = entityManager.find(Company.class, companyId);
// do not clone primary keys for the whole entity subgraph
Company clone = JpaCloner.clone(company, (entity, property) -> !"id".equals(property));
```

##Requirements
- The JPA cloner is tested only against **Hibernate**.
- Cloned entities must use JPA annotations and the _**field access**_, NOT the property access.
- Cloned entities must _**correctly**_ implement equals() and hashCode().

Please refer to the _**JpaCloner**_ class javadoc for more description.
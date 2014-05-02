
# JPA cloner #

The project allows cloning of JPA entity _**subgraphs**_. Entity subgraphs are defined by string patterns or by a custom _PropertyFilter_. The string patterns support operators to allow various expressions of property paths like "company.department+.(boss|employees).address.(country|city|street)". Requirements:

- The JPA cloner is tested only against **Hibernate**.
- Cloned entities must use JPA annotations and the _**field access**_, NOT the property access.
- Cloned entities must _**correctly**_ implement equals() and hashCode().

Maven coordinates:
```xml
<dependency>
    <groupId>com.github.nociar</groupId>
    <artifactId>jpa-cloner</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Changes in version 1.0.0 ##
- project is exposed via the Maven Central Repository
- optimization of relation fetching (OneToMany/ManyToMany before OneToOne)

## Changes in version 0.0.3 ##
- the method **JpaCloner#copy** uses _**setter**_ methods whenever possible (this public method is also used by the cloning process)

## Changes in version 0.0.2 ##
- **JpaCloner#deepClone** methods renamed to **JpaCloner#clone**
- maven groupId changed to **com.github.nociar**
- introduced new methods **JpaCloner#copy**

## Example usage ##
Following examples show cloning by string patterns:

```java
Device device = entityManager.find(Device.class, deviceId);
Device deviceClone = JpaCloner.clone(device, "interfaces");
```

```java
Company company = entityManager.find(Company.class, companyId);
Company companyClone = JpaCloner.clone(company, "department*.employees");
```

Following example shows cloning by a custom property filter:
```java
Company company = entityManager.find(Company.class, companyId);
Company companyClone = JpaCloner.clone(company, new PropertyFilter() {
    @Override
    boolean test(Object entity, String property) {
        // do not clone primary keys for the whole entity subgraph
        return !"id".equals(property);
    }
});
```

Please refer to the _**JpaCloner**_ class javadoc for more description.
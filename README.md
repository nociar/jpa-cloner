
# JPA cloner #

The project allows to clone JPA entity _**subgraphs**_. The subgraphs are defined by _string patterns_ or by a custom _PropertyFilter_. The string patterns support operators to allow various expressions of property paths like "company.department*.(boss|employees).address.(country|city|street)".

## Requirements ##
- The JPA cloner is tested only against **Hibernate**.
- Cloned entities must use JPA annotations and _**field access**_, NOT the property access.
- Cloned entities must _**correctly**_ implement equals() and hashCode().

## Example usage ##
Following example shows cloning by string patterns:

```java
Device device = entityManager.find(Device.class, deviceId);
Device clone = JpaCloner.clone(device, "interfaces");
```

```java
Company company = entityManager.find(Company.class, companyId);
Company clone = JpaCloner.clone(company, "department*.employees");
```

Following example shows cloning by a custom property filter:
```java
Company company = entityManager.find(Company.class, companyId);
Company clone = JpaCloner.deepClone(company, new PropertyFilter() {
    @Override
    boolean test(Object entity, String property) {
        // do not clone primary keys for the whole entity subgraph
        return !"id".equals(property);
    }
});
```

Please refer to the _**JpaCloner**_ class javadoc for more description.
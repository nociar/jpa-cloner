
# JPA cloner #

The project allows cloning of JPA **entity subgraphs**. Entity subgraphs are defined by string patterns.
Cloned entities will have all basic properties copied by default.
Advanced control over the cloning process is supported via the **PropertyFilter** interface.
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
Company clone2 = JpaCloner.clone(company, "depa??ments");
Company clone3 = JpaCloner.clone(company, "departments+.(boss|employees).address");
// do not clone @Id and @Transient fields for the whole entity subgraph:
PropertyFilter filter = PropertyFilters.getAnnotationFilter(Id.class, Transient.class);
Company clone4 = JpaCloner.clone(company, filter, "*+");
```

## Operators
- Dot "." separates paths: A.B.C
- Plus "+" generates at least one preceding path: A.B+.C
- Split "|" divides the path into two ways: A.(B|C).D
- Terminator "$" ends the preceding path: A.(B$|C).D
- Parentheses "(", ")" groups the paths.
- Wildcards "*", "?" in property names: dumm?.pro*ties

##Requirements
- The JPA cloner is tested only against **Hibernate**.
- Cloned entities must **correctly** implement equals() and hashCode().

##Notes
- Property access support is **EXPERIMENTAL**.
- Default access type for non-JPA classes is PROPERTY.
- reading / writing entity values do not strictly adhere AccessType, as without a JPA compatible de-proxying mechanism
the reflective field access would not work properly on lazy fields. Thus the library first tries to use the
getters / setters if they do exist, falls back to field access if they don't, and hopes the best in the latter case.

Please refer to the **JpaCloner** class for more description.
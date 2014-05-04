## 1.0.0
- project is exposed via the Maven Central Repository
- optimization of relation fetching (OneToMany/ManyToMany before OneToOne)

## 0.0.3
- the method **JpaCloner#copy** uses _**setter**_ methods whenever possible (this public method is also used by the cloning process)

## 0.0.2
- **JpaCloner#deepClone** methods renamed to **JpaCloner#clone**
- maven groupId changed to **com.github.nociar**
- introduced new methods **JpaCloner#copy**

## 0.0.1
- first release

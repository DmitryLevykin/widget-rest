# widget-rest
![Java CI with Maven](https://github.com/DmitryLevykin/widget-rest/workflows/Java%20CI%20with%20Maven/badge.svg)

### Widget REST Service

Features:
* Filtering
* Pagination
* Z-index ordering
* Java in-memory or H2 in-memory data storage

Technologies used:
* Maven
* Spring Web
* Spring Boot
* Spring Data JPA
* Hibernate Validator
* H2

#### Run with java in-memory data storage
```bash
$ mvn spring-boot:run
```

#### Run with H2 data storage
```bash
$ mvn spring-boot:run -Drun.profiles=h2-in-memory-storage
```

#### Endpoint
http://localhost:8080/widget

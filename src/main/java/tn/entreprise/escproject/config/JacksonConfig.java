package tn.entreprise.escproject.config;

// Jackson is configured via application.properties:
//   spring.jackson.serialization.fail-on-empty-beans=false
//   spring.jackson.deserialization.fail-on-unknown-properties=false
//
// jackson-datatype-hibernate6 was removed because it is incompatible with
// Hibernate 7 (it references the removed org.hibernate.engine.spi.Mapping
// via ServiceLoader at startup, crashing EntityManagerFactory creation).
// All endpoints return DTOs loaded with JOIN FETCH inside @Transactional,
// so uninitialized lazy proxies never reach the serializer.
public class JacksonConfig {
}

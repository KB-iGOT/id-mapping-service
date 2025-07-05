# Id Mapping Service

A lightweight Spring Boot (3.2.0) microservice that resolves or creates unique integer IDs for string inputs using PostgreSQL. It's ideal for bit position mapping, string-to-ID resolution, and consistent lookup handling.

---

## 🧰 Tech Stack

- Java 17
- Spring Boot 3.2.0
- PostgreSQL
- HikariCP (Connection Pooling)
- Micrometer (Prometheus-compatible metrics)

---

## 🗃️ PostgreSQL Schema

This service relies on the following table:

```sql
CREATE TABLE master_bitposition_lookup (
  id SERIAL PRIMARY KEY,
  name TEXT UNIQUE NOT NULL
);
```

- `id`: Auto-incrementing primary key.
- `name`: Unique string value to be resolved into an ID.

---

## 🔄 SQL Query Logic

The service uses the following SQL query:

```sql
WITH ins AS (
  INSERT INTO master_bitposition_lookup(name)
  VALUES (?)
  ON CONFLICT (name) DO NOTHING
  RETURNING id
)
SELECT id FROM ins
UNION
SELECT id FROM master_bitposition_lookup WHERE name = ?
```

This ensures that either:
- The string is inserted and its new ID returned.
- Or if it already exists, the existing ID is fetched.

This design prevents race conditions during high-concurrency insert-or-get operations.

---

## 📡 REST API

### `GET /idmapping/lookup`

**Purpose**: Lookup or insert a string name and get a unique integer ID.

**Query Parameters**:
- `name` (required): The string name to map.

**Example Request:**
```
GET /idmapping/lookup?name=Group%20A
```

**Success Response:**
- Status: `200 OK`
- Body: `123` (the ID for `Group A`)

**Error Responses:**
- Status: `400 Bad Request` if `name` parameter is missing.

---

## ⚙️ Configuration (application.properties)

```properties
spring.application.name=id-mapping-service
spring.datasource.url=jdbc:postgresql://localhost:5432/bitdb
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1800000
management.endpoints.web.exposure.include=*
management.metrics.enable.all=true
management.endpoint.metrics.enabled=true
idmapping.lookup.query=WITH ins AS (INSERT INTO master_bitposition_lookup(name) VALUES (?) ON CONFLICT (name) DO NOTHING RETURNING id) SELECT id FROM ins UNION SELECT id FROM master_bitposition_lookup WHERE name = ?
```

Environment variables can override any property key.

---

## 🧪 Testing

You can run the tests with:
```bash
./mvnw test
```

Includes unit tests for:
- `IdMappingService`
- `IdMappingController`
- `PropertiesCache`

---

## 🐳 Docker Support

### Dockerfile:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/id-mapping-service-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Build & Run:
```bash
mvn clean package
docker build -t id-mapping-service .
docker run -p 8080:8080 id-mapping-service
```

---

## 📈 Monitoring

Expose metrics to Prometheus using:
```
/actuator/prometheus
```
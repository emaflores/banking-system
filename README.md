## 🏦 Banking System – Microservicios Distribuidos
<span>
Este es un sistema bancario simplificado diseñado con microservicios. Permite gestionar usuarios, cuentas, transferencias y notificaciones mediante una arquitectura moderna y escalable.


## ✨ Características principales

✅ **Arquitectura de microservicios** con Spring Cloud  
✅ **Seguridad robusta** con JWT externalizados y validación de input  
✅ **Resiliencia** con Circuit Breakers, Retry patterns y Timeouts  
✅ **Observabilidad completa** con logging estructurado, métricas Prometheus y tracing  
✅ **Alta disponibilidad** con cache Redis y manejo de errores global  
✅ **Comunicación híbrida** REST síncrona + Kafka asíncrona  
✅ **Testing automatizado** con tests unitarios e integración  
✅ **Configuración externalizada** con variables de entorno  
✅ **API Gateway** con autenticación centralizada y rate limiting  
✅ **Base de datos distribuida** por servicio (PostgreSQL)

---

## 🧩 Microservicios incluidos

| Servicio              | Descripción |
|-----------------------|-------------|
| `discovery-service`   | Eureka Server para descubrimiento dinámico |
| `api-gateway`         | Entrada unificada con filtros JWT y enrutamiento |
| `user-service`        | Registro y autenticación de usuarios |
| `account-service`     | Gestión de cuentas y saldos |
| `transaction-service` | Transferencias entre cuentas |
| `notification-service`| Escucha eventos Kafka y registra logs simulados |

---

## 🧰 Stack tecnológico

### Core
- **Java 17** + **Spring Boot 3.x**
- **Spring Security** + **JWT** externalizados
- **Spring Cloud Gateway** + **Eureka Discovery**
- **Apache Kafka** para mensajería asíncrona
- **PostgreSQL** (instancias separadas por servicio)

### Resiliencia & Performance  
- **Resilience4j** (Circuit Breakers, Retry, TimeLimiter)
- **Redis** para caching distribuido
- **Connection pooling** optimizado

### Observabilidad
- **Logback** con logging estructurado JSON
- **Micrometer** + **Prometheus** para métricas
- **Spring Actuator** para health checks
- **Correlation IDs** para distributed tracing

### Testing & DevOps
- **JUnit 5** + **Mockito** para tests unitarios
- **TestContainers** para integration testing
- **Maven** para build automation
- **Docker Compose** para orquestación local

---

## 🧪 Cómo levantar el sistema completo

### Requisitos

- **Docker** + **Docker Compose**
- **JDK 17**
- **Maven 3.8+**
- **Redis** (para caching)

### Configuración inicial

1. **Configurar variables de entorno:**
```bash
# Copiar el archivo de ejemplo
cp .env.example .env

# Editar las variables según tu entorno
# Como mínimo configurar JWT_SECRET con 32+ caracteres
```

2. **Levantar dependencias externas:**
```bash
# PostgreSQL, Kafka, Zookeeper, Redis
docker-compose -f docker-compose.deps.yml up -d

# O usar Redis standalone
docker run -d -p 6379:6379 --name redis redis:alpine
```

### Pasos de ejecución

```bash
# Clonar el repositorio
git clone https://github.com/emaflores/banking-system.git
cd banking-system

# Instalar dependencias
mvn clean install -DskipTests

# Levantar servicios en orden
# 1. Discovery Service
cd discovery-service && mvn spring-boot:run &

# 2. API Gateway  
cd ../api-gateway && mvn spring-boot:run &

# 3. Servicios de negocio
cd ../user-service && mvn spring-boot:run &
cd ../account-service && mvn spring-boot:run &
cd ../transaction-service && mvn spring-boot:run &
cd ../notification-service && mvn spring-boot:run &
```

⏳ **Esperar 1-2 minutos** mientras Eureka registra los servicios.

---

## 🌐 Endpoints disponibles

### API Gateway
```
http://localhost:8080
```

| Ruta | Servicio | Requiere JWT |
|------|----------|--------------|
| `/auth/register` | user-service | ❌ |
| `/auth/login`    | user-service | ❌ |
| `/accounts/**`   | account-service | ✅ |
| `/transactions/**` | transaction-service | ✅ |

---

## 🧪 Pruebas básicas con Postman/curl

### 1. Registrar usuario
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123",
    "fullName": "Test User"
  }'
```

### 2. Login y obtener token JWT
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com", 
    "password": "Password123"
  }'
# → retorna: { "token": "eyJ...", "email": "...", "fullName": "..." }
```

### 3. Crear cuenta (con token JWT)
```bash
curl -X POST http://localhost:8080/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Realizar transferencia
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "originAccount": "550e8400-e29b-41d4-a716-446655440000",
    "destinationAccount": "550e8400-e29b-41d4-a716-446655440001", 
    "amount": 100.00,
    "currency": "USD"
  }'
```

---

## 📊 Observabilidad y Monitoreo

### Dashboards y endpoints

| Herramienta | URL | Descripción |
|------------|-----|-------------|
| **Eureka Dashboard** | [http://localhost:8761](http://localhost:8761) | Registro de microservicios |
| **Metrics (Prometheus)** | `http://localhost:808{1-4}/actuator/prometheus` | Métricas por servicio |
| **Health Checks** | `http://localhost:808{1-4}/actuator/health` | Estado de servicios |

### Métricas clave disponibles

- `bank_transfers_total` - Total de transferencias
- `bank_transfers_success` - Transferencias exitosas  
- `bank_transfers_failure` - Transferencias fallidas
- `bank_transfer_duration` - Tiempo de procesamiento
- `jvm_memory_used_bytes` - Uso de memoria
- `resilience4j_circuitbreaker_state` - Estado de circuit breakers

### Logs estructurados

```json
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "level": "INFO", 
  "service": "transaction-service",
  "correlationId": "abc-123-def",
  "message": "Processing transfer request from user: user@example.com"
}
```

---

## 🚀 Arquitectura de Producción

Este sistema está diseñado para entornos de producción con:

### ✅ Implementado
- ✅ **Seguridad enterprise** - JWT externalizados, validación robusta
- ✅ **Resiliencia completa** - Circuit breakers, retry, timeouts
- ✅ **Observabilidad 360°** - Logging estructurado, métricas, tracing
- ✅ **Performance optimizada** - Cache Redis, connection pooling
- ✅ **Testing automatizado** - Tests unitarios y de integración
- ✅ **Configuration management** - Variables de entorno externalizadas
- ✅ **Error handling global** - Respuestas estandarizadas
- ✅ **Transacciones distribuidas** - Consistency patterns

### 🔮 Extensiones futuras
- [ ] **API Rate Limiting** con Redis
- [ ] **Saga Pattern** para transacciones distribuidas  
- [ ] **Event Sourcing** completo
- [ ] **Multi-tenancy** support
- [ ] **GraphQL Federation**
- [ ] **Kubernetes deployment** con Helm charts
- [ ] **OAuth2/OIDC** integration
- [ ] **CDC (Change Data Capture)** con Debezium

---

## 🧐 Autor

**Emanuel Flores**  
Backend Developer – Java / Spring Boot / Microservicios


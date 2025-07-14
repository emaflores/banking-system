## 🏦 Banking System – Microservicios Distribuidos

Este es un sistema bancario simplificado diseñado con microservicios. Permite gestionar usuarios, cuentas, transferencias y notificaciones mediante una arquitectura moderna y escalable
---

## ✨ Características principales

✅ Arquitectura de microservicios  
✅ Seguridad con Spring Security + JWT  
✅ Comunicación REST y Kafka (síncrona y asíncrona)  
✅ Eureka para descubrimiento de servicios  
✅ API Gateway con autenticación centralizada  
✅ Base de datos aislada por servicio (PostgreSQL)  
✅ Observabilidad con Spring Actuator y Zipkin  
✅ Docker Compose para levantar toda la plataforma

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

- Java 17 + Spring Boot 3.x
- Spring Security + JWT
- Spring Cloud Gateway + Eureka
- Kafka + Zookeeper
- PostgreSQL (3 instancias separadas)
- Docker + Docker Compose
- Zipkin para tracing
- Maven para build

---

## 🧪 Cómo levantar el sistema completo

### Requisitos

- Docker + Docker Compose
- JDK 17
- Maven

### Pasos

```bash
# Clonar el repositorio
git clone https://github.com/tuusuario/banking-system.git
cd banking-system

# Levantar todo el ecosistema
docker-compose up --build
```

⏳ Esperar unos segundos mientras Eureka registra los servicios.

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

## 🧪 Pruebas básicas con Postman

1. Registrar usuario
```
POST /auth/register
{
  "email": "user@mail.com",
  "password": "1234",
  "fullName": "Test User"
}
```

2. Login y obtener token JWT
```
POST /auth/login
→ retorna: { "token": "..." }
```

3. Crear cuenta (con Authorization: Bearer `<token>`)
```
POST /accounts
```

4. Realizar transferencia
```
POST /transactions
{
  "originAccount": "uuid-1",
  "destinationAccount": "uuid-2",
  "amount": 100.00,
  "currency": "ARS"
}
```

---

## 📆 Observabilidad

| Herramienta | URL |
|------------|-----|
| Eureka Dashboard | [http://localhost:8761](http://localhost:8761) |
| Zipkin (tracing) | [http://localhost:9411](http://localhost:9411) |

---

## ✅ Pendientes / Extensiones posibles

- [ ] Compensaciones de transacciones (Saga pattern)
- [ ] Monitoring con Prometheus + Grafana
- [ ] Tests unitarios e integración
- [ ] Deploy en la nube (Render, Railway, EC2)
- [ ] Web UI (React o Angular)

---

## 🧐 Autor

**Emanuel Flores**  
Backend Developer – Java / Spring Boot / Microservicios


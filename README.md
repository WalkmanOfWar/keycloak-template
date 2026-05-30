# Spring Boot + Keycloak Template

Gotowy do użycia template REST API z integracją Keycloak przez **Spring Security OAuth2 Resource Server (JWT)**.

## Stack

| Technologia | Wersja |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.5 |
| Spring Security | 6.x |
| Keycloak | 25.x |

---

## Szybki start

### 1. Uruchom Keycloak lokalnie

```bash
docker compose up -d
```

Keycloak dostępny pod: `http://localhost:8180`  
Admin: `admin` / `admin`

### 2. Skonfiguruj Keycloak

W panelu admina (`http://localhost:8180/admin`):

1. **Utwórz Realm** → np. `myrealm`
2. **Utwórz Client** → `myclient`
   - Client authentication: **ON** (confidential)
   - Valid redirect URIs: `http://localhost:8080/*`
   - Skopiuj **Client Secret** z zakładki *Credentials*
3. **Utwórz użytkownika** → ustaw hasło w zakładce *Credentials*
4. (Opcjonalnie) **Utwórz Role** → przypisz do użytkownika

### 3. Skonfiguruj aplikację

```bash
cp .env.example .env
# Wypełnij wartości w .env
```

```env
KEYCLOAK_SERVER_URL=http://localhost:8180
KEYCLOAK_REALM=myrealm
KEYCLOAK_CLIENT_ID=myclient
KEYCLOAK_CLIENT_SECRET=<twój-secret>
```

### 4. Uruchom aplikację

```bash
./mvnw spring-boot:run
```

---

## Endpointy

| Metoda | URL | Auth | Opis |
|---|---|---|---|
| GET | `/api/public/health` | ✗ | Health check |
| GET | `/api/user/me` | JWT | Dane zalogowanego użytkownika |
| GET | `/api/user/admin-only` | JWT + role `admin` | Przykład RBAC |
| POST | `/api/auth/token` | ✗ | Pobierz token (dev only) |
| POST | `/api/auth/refresh` | ✗ | Odśwież token |
| POST | `/api/auth/logout` | ✗ | Wyloguj / unieważnij token |

---

## Jak pobrać token (curl)

```bash
curl -s -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"jan","password":"tajne"}' | jq .
```

Użyj `access_token` z odpowiedzi jako Bearer token:

```bash
curl http://localhost:8080/api/user/me \
  -H "Authorization: Bearer <access_token>"
```

---

## Struktura projektu

```
src/
├── main/java/com/example/keycloaktemplate/
│   ├── KeycloakTemplateApplication.java
│   ├── config/
│   │   └── SecurityConfig.java          ← konfiguracja Spring Security
│   ├── security/
│   │   ├── KeycloakJwtConverter.java    ← mapowanie ról z JWT na GrantedAuthority
│   │   └── CurrentUser.java             ← model zalogowanego użytkownika
│   ├── controller/
│   │   ├── AuthController.java          ← token / refresh / logout proxy
│   │   ├── UserController.java          ← /me i przykład RBAC
│   │   └── PublicController.java        ← publiczne endpointy
│   └── exception/
│       └── GlobalExceptionHandler.java  ← RFC 9457 ProblemDetail błędy
└── resources/
    └── application.yml
```

---

## Dodawanie nowych chronionych endpointów

```java
@GetMapping("/api/orders")
@PreAuthorize("hasRole('user')")      // wymaga roli "user" w Keycloak
public ResponseEntity<List<Order>> getOrders(@AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject(); // Keycloak UUID użytkownika
    // ...
}
```

---

## Testy

```bash
./mvnw test
```

Testy używają `MockMvc` z `spring-security-test` — **nie wymagają działającego Keycloak**.

---

## Zmiana package name

Zamień `com.example.keycloaktemplate` na swoją paczkę we wszystkich plikach Java.

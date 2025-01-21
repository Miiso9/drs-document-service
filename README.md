# Distribuirani Racunalni Sustavi - Zadaca

## Izrada backenda za razmjenu elektronskih dokumenata

Napraviti RESTful API koji će moći zaprimiti dokumente, a onda te iste dokumente prosljediti u primateljev inbox.

### Faze projekta:
## 1.Izrada API-ja
Cilj je napraviti RESTful API za upravljanje dokumentima, uključujući prijenos, pohranu i prosljeđivanje.
  - Endpointi:
    - POST /documents: Prima dokumente za pohranu.
    - GET /documents/{id}: Dohvaća detalje o dokumentu.
    - POST /documents/{id}/send: Prosljeđuje dokument na e-mail primatelja.
  - Osnovne Karakteristike:
    - Primanje datoteka (npr. PDF, DOCX) putem multipart/form-data zahtjeva.
    - Pohranjivanje dokumenata u lokalni sustav datoteka ili vanjsku pohranu (npr. S3, Google Cloud Storage).
    - Integracija sa SMTP serverom ili e-mail servisom poput SendGrid za slanje dokumenata.

## 2. Autentifikacija i autorizacija
Za sigurnost API-ja treba implementirati autentifikaciju i autorizaciju.
 - JWT (JSON Web Token):
   - Koristi se za autentifikaciju korisnika.
   - Token se generira pri prijavi i šalje s API zahtjevima u Authorization zaglavlju.
 - Role-based Access Control (RBAC):
   - Korisnici mogu imati različite razine pristupa (npr. običan korisnik, administrator).
   - POST i GET zahtjevi prema dokumentima trebaju biti ograničeni u skladu s ulogama korisnika.


# Pokretanje backend servica
## Preduvjeti:
- Java 17
- Maven
- Docker
- SendGrid Account

## komponente
- Spring Boot
- Localstack (S3)
- SendGrid (Mail provider)
- PostgreSQL
- Keycloak (Auth)

## Konfiguracija

Potrebno je imati SendGrid račun kako bi smo dobili API koji koristimo u varijabli okruzenja `SENDGRID_API` (`application.properties`).
Nakon toga na SendGrid računu potrebno je autenticirati pošiljatelj e-adresu u izborniku Pošiljatelj Autentikaciju, koju koristimo u varijabli okruženja `SENDER_EMAIL` (`application.properties`).
Ostale varijable mogu koristit "default" vrijednosti.

## Pokretanje

1. U terminal u root direktoriju pokrenuti docker-compose-yml:
```
docker compose up
```
2. Pokrenuti Spring Boot servis
3. Na web pretraživač otvoriti `http://localhost:8080/swagger-ui/index.html`
4. Izgenerirati JWT token na dva nacina:
- Putem endpointa `/api/auth/login`.
  - username: admin
  - password: adminpassword
- Poziv na keycloak client `http://localhost:8081/realms/DRS/protocol/openid-connect/token`
  - ```
    curl --location 'http://localhost:8081/realms/DRS/protocol/openid-connect/token' \
    --header 'accept: application/vnd.ms-excel' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=password' \
    --data-urlencode 'client_id=drs-document' \
    --data-urlencode 'username=admin' \
    --data-urlencode 'password=adminpassword' \
    --data-urlencode 'client_secret=client-secret'
    ```
5. Dobiveni `accesToken` kopiraj i ubaci vrijednost u swagger authorize
6. Test endpoint


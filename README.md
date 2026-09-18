# Modia Robot Api

Dedikerte apier for robotbrukere

Swagger:
Dev: [https://modia-robot-api.intern.dev.nav.no/swagger-ui](https://modia-robot-api.intern.dev.nav.no/swagger-ui)
Prod: [https://modia-robot-api.intern.nav.no/swagger-ui](https://modia-robot-api.intern.nav.no/swagger-ui)

Open API spec ligger også versjonert i [`docs/openapi.json`](docs/openapi.json) og kan deles direkte med
konsumentene. Den serveres også fra applikasjonen på `/openapi.json`, med Swagger-UI på
`/swagger-ui`.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

[Team Personoversikt](https://github.com/navikt/info-team-personoversikt)

## Utvikling

### Kjør lokalt

Kjør Main() i `RunLocally.kt`

### Oppdatering av Open API spesifikasjon

Open API spec ligger versjonert i [`docs/openapi.json`](docs/openapi.json) og kan deles direkte med
konsumentene. Den serveres også fra applikasjonen på `/openapi.json`, med Swagger-UI på
`/swagger-ui`.

`OpenApiKontraktTest` sammenligner spesifikasjonen bygget fra rutene med den versjonerte filen, slik
at en kontraktsendring blir synlig i pull request-diffen i stedet for å oppdages av konsumentene i
produksjon. Testene vil feile dersom spesifikasjonen blir endret.
Dersom du har med intensjon endret på spesifikasjonen kan du oppdatere den med kommandoen:

```
./gradlew test -PoppdaterOpenapi=true
```
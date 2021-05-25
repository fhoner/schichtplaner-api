# Schichtplaner ![build status](https://github.com/fhoner/schichtplaner-api/actions/workflows/build.yaml/badge.svg)

_**Under development**_

## Tech stack

- Kotlin
- Spring Boot
- Expedia's graphql-kotlin
- PostgreSQL

## Run locally

To run the Spring Boot application:

`./gradlew bootRun`

To insert test data after application is up:

`./gradlew bootRun --args='--spring.profiles.active=testdata'`

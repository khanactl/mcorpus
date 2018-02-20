# mcorpus
A GraphQL server endpoint POC written in Java 8.

A Maven-based project with three sub-projects:
- mcorpus-gql
- mcorpus-db
- mcorpus-jwtgen

## mcorpus-gql
The MCorpus GraphQL server endpoint jar app.

## mcorpus-db
Houses the JooQ generated types representing the db-schema and data access api.  Used by mcorpus-gql.

## mcorpus-jwtgen
Standalone jar that generates time-sensitive JWT tokens for use against the MCorpus GraphQL web interface.

# Features
- JWT for user authentication via http-only cookie
- CSRF protection against the GraphQL endpoint (/graphql) via per-request synchronization tokens
- GraphiQL (intended as a developer interface to understand the mcorpus GraphQL schema)
- asynchronous graphql-java data fetchers
- Pacj4 and ratpack-pac4j for managing server-side user sessions and user authentication
- Postgres db for housing the mcorpus data repository
- Minimalist first-principles approach
- Powered by 
  - [Ratpack](https://ratpack.io/) / [Netty](https://netty.io/)
  - [JooQ](https://www.jooq.org/)
  - [Postgres](https://www.postgresql.org/)
  - [graphql-java](https://github.com/graphql-java/graphql-java)

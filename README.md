# mcorpus
A GraphQL server endpoint POC written in Java 8.

A Maven-based Java project with two sub-projects:
- mcorpus-gql
- mcorpus-db

## mcorpus-gql
The MCorpus GraphQL server endpoint jar app.

## mcorpus-db
Houses the JooQ generated types representing the db-schema and data access api.  Used by mcorpus-gql.

# Features
- Web session based user login with CSRF protection
- JWT, signed and ecnrypted, for user authentication via http-only cookie
- CSRF protection against the GraphQL endpoint (/graphql) via per-request synchronization tokens
- GraphiQL (the developer, schema-discovery UI to understand the mcorpus GraphQL schema)
- Asynchronously driven with Java 8
- Postgres db for the backend data repository
- Minimalist first-principles approach
- Powered by 
  - [Ratpack](https://ratpack.io/) / [Netty](https://netty.io/)
  - [JooQ](https://www.jooq.org/)
  - [Postgres](https://www.postgresql.org/)
  - [graphql-java](https://github.com/graphql-java/graphql-java)

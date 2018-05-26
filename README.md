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
- JWT, signed and ecnrypted, for token-based user authentication via secure, http-only cookies.
- Per-request sync tokens (stateless) for every GraphQL (/graphql) request to mitigate CSRF attacks.
- GraphiQL UI - the 'schema discovery' UI as a means to issue GraphQL requests and understand the mcorpus schema.
- Postgres db for the backend data repository
- Minimalist first-principles approach
- Powered by 
  - [Postgres](https://www.postgresql.org/)
  - [JooQ](https://www.jooq.org/)
  - [graphql-java](https://github.com/graphql-java/graphql-java)
  - [Ratpack](https://ratpack.io/) / [Netty](https://netty.io/)

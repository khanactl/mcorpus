# mcorpus

A robust GraphQL server endpoint POC written in Java 8.

A Maven-based Java project with two sub-projects:

- **mcorpus-gql**
  The MCorpus GraphQL server endpoint packaged as an über jar.
- **mcorpus-db**
  Houses the JooQ generated types representing the db-schema and data access api in the form of Java classes. Used by mcorpus-gql.

## docs

Checkout the [wiki](https://github.com/khanactl/mcorpus/wiki) pages for design details..

## Features

- GraphQL-based user login by username and password.
- JWT (signed and encrypted) via secure, http-only cookies upon successful user login.
- Per-request sync token verification for all inbound GraphQL requests to mitigate CSRF attacks.
- GraphiQL - the 'schema discovery' UI intended for developers to introspect the schema and issue GraphQL requests from a web UI.
- Lean and performant Java 8 code in a fully asynchronous paradigm realized by best-of-breed stack:
  - [Postgres](https://www.postgresql.org/)
  - [JooQ](https://www.jooq.org/)
  - [graphql-java](https://github.com/graphql-java/graphql-java)
  - [Ratpack](https://ratpack.io/) / [Netty](https://netty.io/)

## Demo on AWS

Check out a working demo on [AWS](https://mcorpusgql-dev.net).
![GraphiQL-demo](docs/graphiql.png "mcorpus GraphiQL interface")

- First click Login then use username = 'demo' and password = 'password23'. You will then be logged in based on a JWT cookie for 2 days.
- Then click the GraphiQL link. From there, you may freely issue GraphQL queries through the standard GraphiQL interface.
- All mcorpus data was generated purely randomly and is completely fictitous meant only for demonstrating a Java/Netty based, fully asynchronous, highly performant GraphQL endpoint.

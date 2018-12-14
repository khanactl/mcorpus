# mcorpus
A GraphQL server endpoint POC written in Java 8.

A Maven-based Java project with two sub-projects:
- mcorpus-gql
- mcorpus-db

## mcorpus-gql
The MCorpus GraphQL server endpoint packaged as an über jar (16M at present).

## mcorpus-db
Houses the JooQ generated types representing the db-schema and data access api in the form of Java classes.  Used by mcorpus-gql.

# docs
Checkout the [wiki](https://github.com/khanactl/mcorpus/wiki) pages for design details..

# Features
- Web session based user username/password login with CSRF protection.
- JWT, signed and ecnrypted, for token-based user authentication via secure, http-only cookies.
- Per-request sync tokens for every GraphQL request to mitigate CSRF attacks.
- GraphiQL UI - the 'schema discovery' UI intended for developers to introspect the schema and issue GraphQL requests from a web UI.
- Postgres db for the backend data repository.
- Slim, lean and performant Java code in a fully asynchronous paradigm realized by [Ratpack](https://ratpack.io) 
- Best of breed stack: 
  - [Postgres](https://www.postgresql.org/)
  - [JooQ](https://www.jooq.org/)
  - [graphql-java](https://github.com/graphql-java/graphql-java)
  - [Ratpack](https://ratpack.io/) / [Netty](https://netty.io/)
	
# Demo on AWS
Check out a working demo on [AWS](https://www.mcorpus-aws.net)!
- First click Login then use username = 'jp' and password = 'jackson'. You will then be logged in based on a JWT cookie for 2 days.
- Then click the GraphiQL link.  From there, you may freely issue GraphQL queries through the standard GraphiQL interface.

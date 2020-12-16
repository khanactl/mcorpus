<script lang="ts">
	import successkid from 'images/successkid.jpg';

  // Apollo client
  import fetch from 'cross-fetch';
  import { ApolloClient, createHttpLink, InMemoryCache } from '@apollo/client';
  import { setContext } from '@apollo/client/link/context';

  const httpLink = createHttpLink({
    uri: '/graphql',
    fetch: fetch,
  });

  const authLink = setContext((_, { headers }) => {
    // get the authentication token from local storage if it exists
    const token = localStorage.getItem('token');
    // return the headers to the context so httpLink can read them
    return {
      headers: {
        ...headers,
        authorization: token ? `Bearer ${token}` : "",
      }
    }
  });

  export const apolloClient = new ApolloClient({
    link: authLink.concat(httpLink),
    cache: new InMemoryCache(),
  });
  // END Apollo client

</script>

<style>
  label {
    cursor: pointer;
    width: 80px;
    display: inline-block;
  }

	h1, figure, p {
		text-align: center;
		margin: 0 auto;
	}

	h1 {
		font-size: 2.8em;
		text-transform: uppercase;
		font-weight: 700;
		margin: 0 0 0.5em 0;
	}

	figure {
		margin: 0 0 1em 0;
	}

	img {
		width: 100%;
		max-width: 400px;
		margin: 0 0 1em 0;
	}

	p {
		margin: 1em auto;
	}

	@media (min-width: 480px) {
		h1 {
			font-size: 4em;
		}
	}
</style>

<svelte:head>
	<title>Sapper project template</title>
</svelte:head>

<h1>Great success!</h1>

<figure>
	<img alt="Success Kid" src="{successkid}">
	<figcaption>Have fun with Sapper!</figcaption>
</figure>

<div>
  <form>
    <div>
      <label for="username">Username</label>
      <input type="text" id="username" maxlength="30" />
    </div>
    <div>
      <label for="password">Password</label>
      <input type="password" id="password" maxlength="30" />
    </div>
  </form>
</div>

<p><strong>Try editing this file (src/routes/index.svelte) to test live reloading.</strong></p>

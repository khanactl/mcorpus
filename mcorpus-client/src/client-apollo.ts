// Apollo client
import { ApolloClient, ApolloLink, createHttpLink, from, InMemoryCache, ServerError } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import { onError } from '@apollo/client/link/error';
import fetch from 'cross-fetch';

const httpLink = createHttpLink({
  uri: 'http://localhost:5150/graphql',
  fetch: fetch,
  credentials: 'include',
});

const authLink = setContext((_, { headers }) => {
  // get the authentication jwt from local storage if it exists
  const jwt = localStorage.getItem('jwt');
  const rst = localStorage.getItem('rst');
  // return the headers to the context so httpLink can read them
  return {
    headers: {
      ...headers,
      rst: rst,
      authorization: jwt ? `Bearer ${jwt}` : "",
    }
  }
});

const errorLink = onError( ({ graphQLErrors, networkError, operation, forward }) => {
  if (graphQLErrors)
    graphQLErrors.map(({ message, locations, path }) =>
      console.log(
        `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`
      )
    );
  if (networkError) {
    console.log(`[Network error]: ${networkError}`);
    const nerror = networkError as ServerError;
    // console.log(`[Network error] status code: ${nerror.statusCode}`);
    if (nerror.statusCode === 205) {
      const { response } = operation.getContext();
      const { headers } = response;
      // console.log('response headers: ' + JSON.stringify(headers));
      const rst = headers.map['rst'];
      // console.log('retry rst: ' + rst);
      operation.setContext({
        headers: {
          rst: rst,
        }
      });
    }
  }
  // console.log('error link end');
  return forward(operation);
});

// const retryLink = new RetryLink();

const responseLink = new ApolloLink((operation, forward) => {
  return forward(operation).map(result => {
    const headers = operation.getContext().response.headers;
    // console.log('responseLink response headers: ' + JSON.stringify(headers));
    const rst = headers.map['rst'];
    // console.log('responseLink rst: ' + rst);
    if(rst) localStorage.setItem('rst', rst);
    return result;
  });
});

const apolloClient = new ApolloClient({
  link: from([
    authLink,
    responseLink,
    // retryLink,
    errorLink,
    httpLink
  ]),
  cache: new InMemoryCache(),
});

export default apolloClient;


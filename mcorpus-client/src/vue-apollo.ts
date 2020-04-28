import Vue from "vue";
import VueApollo from "vue-apollo";
import { createApolloClient, restartWebsockets } from "vue-cli-plugin-apollo/graphql-client";

// add module import and define apolloClient type
import { ApolloClient } from "apollo-client";
import { ApolloLink } from "apollo-link";
import { onError } from "apollo-link-error";
import { SubscriptionClient } from "subscriptions-transport-ws";
import { InMemoryCache } from "apollo-cache-inmemory";
import { ServerError, ServerParseError } from "apollo-link-http-common";
import { setContext } from "apollo-link-context";

// Install the vue plugin
Vue.use(VueApollo);

export type VueApolloClient = ApolloClient<InMemoryCache> & {
  wsClient: SubscriptionClient;
};

// Name of the localStorage item
const AUTH_TOKEN = "apollo-token";

// Http endpoint
const httpEndpoint = process.env.VUE_APP_GRAPHQL_HTTP || "http://localhost:5150/graphql";
// Files URL root
export const filesRoot = process.env.VUE_APP_FILES_ROOT || httpEndpoint.substr(0, httpEndpoint.indexOf("/graphql"));

Vue.prototype.$filesRoot = filesRoot;

export function isServerError(err: Error | ServerError | ServerParseError): err is ServerError {
  return (err as ServerError).statusCode !== undefined;
}

export function isServerParseError(err: Error | ServerError | ServerParseError): err is ServerParseError {
  return (err as ServerParseError).statusCode !== undefined;
}

export function getLocalRst(): string | null {
  const rst = localStorage.getItem("rst");
  console.log("getLocalRst - rst: " + rst);
  return rst;
}

export function setLocalRst(rst: string): void {
  localStorage.setItem("rst", rst);
  console.log("setLocalRst - rst: " + rst);
}

const rstSyncLink = onError(({ graphQLErrors, networkError, operation, forward }) => {
  if (networkError) {
    console.log(`rstSyncLink - network error: ${networkError}`);
    if (isServerError(networkError) || isServerParseError(networkError)) {
      console.log("networkError.statusCode: " + networkError.statusCode);
      if (networkError.statusCode == 205) {
        // grab rst from response and put in request before re-fetch
        const rst = networkError.response.headers.get("rst");
        console.log("rstSyncLink - rst: " + rst);
        if (rst) {
          // localStorage.setItem("rst", rst);
          const oldHeaders = operation.getContext().headers;
          console.log(`oldHeaders: ${oldHeaders}`);
          operation.setContext({
            credentials: "include",
            headers: {
              ...oldHeaders,
              rst: rst,
            },
          });
          return forward(operation); // retry
        }
      }
    }
  }
});

const rstUpdateLink = setContext((request, previousContext) => ({
  headers: { rst: getLocalRst() },
}));

// after processing link for rst syncing
const afterwareLink = new ApolloLink((operation, forward) => {
  console.log("afterware link");
  return forward(operation).map((response) => {
    const context = operation.getContext();
    const {
      response: { headers },
    } = context;
    if (headers) {
      const rst = headers.get("rst");
      console.log("rst (afterware): " + rst);
      if (rst) setLocalRst(rst);
    }

    return response;
  });
});

// Config
const defaultOptions = {
  // You can use `https` for secure connection (recommended in production)
  httpEndpoint,
  // You can use `wss` for secure connection (recommended in production)
  // Use `null` to disable subscriptions
  // wsEndpoint: process.env.VUE_APP_GRAPHQL_WS || "ws://localhost:4000/graphql",
  wsEndpoint: null,
  // LocalStorage token
  tokenName: AUTH_TOKEN,
  // Enable Automatic Query persisting with Apollo Engine
  persisting: false,
  // Use websockets for everything (no HTTP)
  // You need to pass a `wsEndpoint` for this to work
  websocketsOnly: false,
  // Is being rendered on the server?
  ssr: false,

  // Override default apollo link
  // note: don't override httpLink here, specify httpLink options in the
  // httpLinkOptions property of defaultOptions.
  link: rstUpdateLink.concat(rstSyncLink).concat(afterwareLink),
  httpLinkOptions: {
    credentials: "include",
  },

  // Override default cache
  // cache: myCache

  // Override the way the Authorization header is set
  // getAuth: (tokenName) => ...
  // since JWT is sent via cookies, getAuth() does not need to get a token from the authorization header
  getAuth: () => {
    return undefined;
  },

  // Additional ApolloClient options
  // apollo: { ... }

  // Client local data (see apollo-link-state)
  // clientState: { resolvers: { ... }, defaults: { ... } }
};

// Call this in the Vue app file
export function createProvider(options = {}) {
  // Create apollo client
  const { apolloClient, wsClient } = createApolloClient({
    ...defaultOptions,
    ...options,
  });
  apolloClient.wsClient = wsClient;

  // Create vue apollo provider
  const apolloProvider = new VueApollo({
    defaultClient: apolloClient,
    defaultOptions: {
      $query: {
        // fetchPolicy: 'cache-and-network',
      },
    },
    errorHandler(error) {
      // eslint-disable-next-line no-console
      console.log(
        "%cError",
        "background: red; color: white; padding: 2px 4px; border-radius: 3px; font-weight: bold;",
        error.message
      );
    },
  });

  return apolloProvider;
}

// Manually call this when user log in
export async function onLogin(apolloClient: VueApolloClient, token: string) {
  console.log("onLogin");
  if (typeof localStorage !== "undefined" && token) {
    localStorage.setItem(AUTH_TOKEN, token);
  }
  if (apolloClient.wsClient) restartWebsockets(apolloClient.wsClient);
  try {
    await apolloClient.resetStore();
  } catch (e) {
    // eslint-disable-next-line no-console
    console.log("%cError on cache reset (login)", "color: orange;", e.message);
  }
}

// Manually call this when user log out
export async function onLogout(apolloClient: VueApolloClient) {
  console.log("onLogout");
  if (typeof localStorage !== "undefined") {
    localStorage.removeItem(AUTH_TOKEN);
  }
  if (apolloClient.wsClient) restartWebsockets(apolloClient.wsClient);
  try {
    await apolloClient.resetStore();
  } catch (e) {
    // eslint-disable-next-line no-console
    console.log("%cError on cache reset (logout)", "color: orange;", e.message);
  }
}

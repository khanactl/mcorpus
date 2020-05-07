import Vue from "vue";
import Vuex from "vuex";
import { apolloClient } from "@/vue-apollo";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    jwtUser: {},
    authStatus: false,
  },
  getters: {
    isAuthenticated: (state) => state.authStatus === true,
    jwtUser: (state) => state.jwtUser,
  },
  mutations: {
    LOGIN_USER(state, jwtUser) {
      state.authStatus = true;
      state.jwtUser = { ...jwtUser };
    },
    LOGOUT_USER(state) {
      state.authStatus = false;
      state.jwtUser = {};
      localStorage.removeItem("apollo-token");
    },
  },
  actions: {
    async login({ commit, dispatch }, authDetails) {
      try {
        const { data } = await apolloClient.mutate({
          mutation: require("../graphql/JWTLogin.gql"),
          variables: { ...authDetails },
        });
        const loginSuccess = data.jwtLogin != null;
        console.log("store.login - loginSuccess: " + loginSuccess);
        if (loginSuccess) {
          commit("LOGIN_USER", {
            uid: data.jwtLogin.uid,
            name: data.jwtLogin.jwtUserName,
            username: data.jwtLogin.jwtUserUsername,
            email: data.jwtLogin.jwtUserEmail,
          });
        }
      } catch (e) {
        console.log(e);
      }
    },
    async logout({ commit, dispatch }) {
      try {
        const { data } = await apolloClient.mutate({
          mutation: require("../graphql/JWTLogout.gql"),
        });
        const logoutSuccess = data.jwtLogout;
        console.log("store.logout - logoutSuccess: " + logoutSuccess);
        commit("LOGOUT_USER");
      } catch (e) {
        console.log(e);
      }
    },
  },
  modules: {},
});

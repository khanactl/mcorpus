<template>
  <div id="app">
    <!--
    <div id="nav">
      <router-link to="/">Home</router-link> | <router-link to="/about">About</router-link> |
      <router-link to="/login">Login</router-link>
    </div>
    -->
    <header class="header">
      <div class="app-name">mcorpus</div>
      <div v-if="isLoggedIn" id="nav">
        <div>{{ jwtUser.username }}</div>
        <button class="auth-button" @click="logout">Log Out</button>
      </div>
    </header>
    <router-view />
  </div>
</template>

<script>
import { mapGetters } from "vuex";
export default {
  methods: {
    logout: function () {
      this.$store.dispatch("logout").then(() => {
        this.$router.push("/login");
      });
    },
  },
  computed: {
    ...mapGetters(["jwtUser"]),
    isLoggedIn() {
      return this.$store.getters.isAuthenticated;
    },
  },
};
</script>

<style>
@import "./assets/main.css";
#app {
  font-family: Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>

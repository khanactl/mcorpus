<template>
  <div class="auth">
    <h3>Login</h3>
    <p v-if="loginError" class="error">Login unsuccessful.</p>
    <form action="POST" @submit.prevent="loginUser">
      <label for="username" class="label">Username</label>
      <input v-model="authDetails.username" placeholder="username" class="input" id="username" />
      <label for="password" class="label">Password</label>
      <input type="password" v-model="authDetails.pswd" placeholder="password" class="input" id="password" />
      <button class="auth-submit">submit</button>
    </form>
  </div>
</template>

<script>
import { mapActions } from "vuex";
export default {
  name: "Login",
  data() {
    return {
      loginError: false,
      authDetails: {
        username: "",
        pswd: "",
      },
    };
  },

  methods: {
    ...mapActions(["login"]),
    loginUser() {
      this.login(this.authDetails).then(() => {
        console.log("JWTLogin - post login block..");
        this.loginError = !this.$store.getters.isAuthenticated;
        if (!this.loginError) {
          console.log("login ok - routing to home..");
          this.$router.push("/home");
        }
      });
    },
  },
};
</script>

<style scoped>
.form,
.input,
.apollo,
.message {
  padding: 12px;
}

label {
  display: block;
  cursor: pointer;
  margin-bottom: 6px;
}

.input {
  font-family: inherit;
  font-size: inherit;
  border: solid 2px #ccc;
  border-radius: 3px;
}

.error {
  color: red;
}
</style>

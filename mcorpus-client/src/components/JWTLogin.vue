<template>
  <div class="login">
    <ApolloMutation
      :mutation="require('../graphql/JWTLogin.gql')"
      :variables="{
        username: username,
        pswd: pswd,
      }"
      @done="onLoginResponse"
      @error="onLoginError"
    >
      <template slot-scope="{ mutate, loading, error }">
        <form>
          <label for="username" class="label">Username</label>
          <input v-model="username" placeholder="username" class="input" id="username" />
          <label for="password" class="label">Password</label>
          <input type="password" v-model="pswd" placeholder="password" class="input" id="password" />
          <button :disabled="loading" @click="mutate()">Submit</button>
        </form>
        <p v-if="error">An error occurred: {{ error }}</p>
      </template>
    </ApolloMutation>
  </div>
</template>

<script>
import { onLogin } from "../vue-apollo";
export default {
  data() {
    return {
      username: "demo",
      pswd: "",
    };
  },

  apollo: {
    // files: FILES
  },

  computed: {},

  methods: {
    onLoginError(err) {
      console.log(err);
    },
    onLoginResponse(resp) {
      // login successful?
      console.log(resp);
      const loginSuccess = resp && resp.data && resp.data.jwtLogin ? resp.data.jwtLogin : false;
      console.log("login success? " + loginSuccess);
      if (loginSuccess) {
        onLogin(this.$apollo.provider.defaultClient, "TODO-authToken");
      }
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

.images {
  display: grid;
  grid-template-columns: repeat(auto-fill, 300px);
  grid-auto-rows: 300px;
  grid-gap: 10px;
}

.image-item {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ccc;
  border-radius: 8px;
}

.image {
  max-width: 100%;
  max-height: 100%;
}

.image-input {
  margin: 20px;
}
</style>

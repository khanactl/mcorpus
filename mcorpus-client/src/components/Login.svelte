<script lang="ts">
  import { gql } from '@apollo/client';
  import apolloClient from '../client-apollo';

  let username = "";
  let password = "";
  let statusMsg = "";
  let jwt = "";
  let jwtExpires = "";

  const GLOGIN = gql`
    mutation JwtLogin($username: String!, $password: String!) {
      jwtLogin(username: $username, pswd: $password) {
        jwt,
        jwtExpires,
        errorMsg
      }
    }
  `;

  async function handleSubmit() {
    apolloClient.mutate({
      mutation: GLOGIN,
      variables: { username: username, password: password },
    })
    .then(fetchResult => {
      console.log(`fetchResult: ${JSON.stringify(fetchResult)}`)
      if(fetchResult.errors) {
        statusMsg = fetchResult.errors.join(", ");
      } else {
        const jwtLoginResult = fetchResult.data.jwtLogin;
        const errorMsg = jwtLoginResult.errorMsg;
        console.log('errorMsg: ' + errorMsg);
        if(errorMsg && errorMsg.length > 0) {
          statusMsg = errorMsg;
        } else {
          statusMsg = "Login success";
          jwt = jwtLoginResult['jwt'];
          jwtExpires = jwtLoginResult['jwtExpires'];

          // cache jwt
          localStorage.setItem('jwt', jwt);
        }
      }
    })
    .catch(err => {
      statusMsg = err;
      console.log(err);
    });
  }
</script>

<style>
  label {
    cursor: pointer;
    width: 80px;
    display: inline-block;
  }
  .statusMsg {
    color: red;
  }
</style>

<login>
  <div>
    <div class="statusMsg">{jwt}</div>
    <div class="statusMsg">{jwtExpires}</div>

    <div class="statusMsg">{statusMsg}</div>
    <form on:submit|preventDefault="{handleSubmit}">
      <div>
        <label for="username">Username</label>
        <input type="text" id="username" maxlength="30" bind:value="{username}" />
      </div>
      <div>
        <label for="password">Password</label>
        <input type="password" id="password" maxlength="30"  bind:value="{password}" />
      </div>
      <div><button type="submit">Login</button></div>
    </form>
  </div>
</login>

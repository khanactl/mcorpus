<template>
  <div class="">
    <JWTLogin />
  </div>
</template>

<script>
// @ is an alias to /src
import JWTLogin from "@/components/JWTLogin.vue";

export default {
  name: "login",
  components: {
    JWTLogin,
  },
};
</script>

<script setup lang="ts">
import { ref } from 'vue';
import * as Stomp from "@stomp/stompjs";
import axios from "axios";
import KeyCloakService from './services/KeycloakService';

const result = ref<string>("");
const connected = ref<boolean>(false);
const name = ref<string>("");
const shake = ref<boolean>(false);

function sendGrettings(): void {
  stompClient.publish({destination: '/app/hello', body: name.value})
}

function logout(): void {
    KeyCloakService.keycloakInstance.logout();
}

function doRestCall(): void {
  axios.get("http://localhost:7100/external", {headers: {
    Authorization: 'Bearer ' + KeyCloakService.keycloakInstance.token
  }})
  .then(() => document.getElementById("rest-call")!.style.backgroundColor = "green")
  .catch(() => document.getElementById("rest-call")!.style.backgroundColor = "red")
}

// TODO: make the websocket call working with access_token
const stompClient = new Stomp.Client({
  brokerURL: 'ws://localhost:7100/websocket?access_token=' + KeyCloakService.keycloakInstance.token,
  onConnect() {
      connected.value = true;
      stompClient.subscribe('/topic/greetings', (message: Stomp.IMessage) => {
        result.value = message.body;
      });
      stompClient.subscribe('/alert/trigger', () => {
        shake.value = true;
        setTimeout(() => {
          shake.value = false;
        }, 2000)
      })
  }
});

stompClient.activate();

</script>

<template>
  <div class="top-right">
    <button type="submit" style="margin-right: 2rem;" @click="logout()">Logout</button>
    <button id="rest-call" type="submit" @click="doRestCall()">Rest call</button>
  </div>
  <div>
    <img src="/vite.svg" class="logo" :class="{shake: shake}" alt="Vite logo" />
    <img src="./assets/vue.svg" class="logo vue" :class="{shake: shake}" alt="Vue logo" />
  </div>
  <div v-if="!connected">Waiting for websocket connection</div>
  <button v-else class="button" type="submit" @click="sendGrettings()">Send grettings to: </button><input v-model="name" @keyup.enter="sendGrettings()" placeholder="Name to be greeted">
  <br>
  <br>
  Results: {{  result }}
</template>

<style scoped lang="css">
.top-right {
  position: fixed;
  top: 3%;
  right: 3%;
}
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}

.button {
  margin-right: 2rem;
}

.shake {
  animation: shake 0.82s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
  transform: translate3d(0, 0, 0);
}

@keyframes shake {
  10%,
  90% {
    transform: translate3d(-2px, 0, 0);
  }

  20%,
  80% {
    transform: translate3d(4px, 0, 0);
  }

  30%,
  50%,
  70% {
    transform: translate3d(-8px, 0, 0);
  }

  40%,
  60% {
    transform: translate3d(8px, 0, 0);
  }
}
</style>

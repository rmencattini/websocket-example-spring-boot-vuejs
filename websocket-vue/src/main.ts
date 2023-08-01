import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import KeyCloakService from './services/KeycloakService'

KeyCloakService.CallLogin(() => createApp(App).mount('#app'))

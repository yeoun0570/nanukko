import { createApp } from "vue";
import App from "./App.vue";
import routers from "./routers/index";
import { createPinia } from "pinia";
import { globalCookiesConfig } from "vue3-cookies";

import "./assets/main.css";

globalCookiesConfig({
	expireTimes: "30d",
	path: "/",
	domain: "",
	secure: true,
	sameSite: "None",
});

const pinia = createPinia();

createApp(App).use(routers).use(pinia).mount("#app");

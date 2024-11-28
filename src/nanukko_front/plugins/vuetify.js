import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import { aliases, mdi } from "vuetify/iconsets/mdi";

export default defineNuxtPlugin((nuxtApp) => {
	const vuetify = createVuetify({
		components,
		directives,
		icons: {
			defaultSet: "mdi", // 기본 아이콘 세트를 'mdi'로 설정
			aliases,
			sets: {
				mdi,
			},
		},
	});

	nuxtApp.vueApp.use(vuetify);
});

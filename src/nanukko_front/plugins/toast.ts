export default defineNuxtPlugin(() => {
	return {
		provide: {
			showToast: (mainMessage: string) => {
				const toast = document.createElement("div");
				toast.className = "notification-toast";
				toast.innerHTML = `
            <div class="toast-content">
              <div class="toast-message">${mainMessage}</div>
            </div>
          `;
				document.body.appendChild(toast);
				setTimeout(() => {
					toast.classList.add("show");
					setTimeout(() => {
						toast.classList.remove("show");
						setTimeout(() => {
							document.body.removeChild(toast);
						}, 300);
					}, 3000);
				}, 100);
			},
		},
	};
});

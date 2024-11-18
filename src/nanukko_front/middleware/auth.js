// middleware/auth.global.ts (또는 .js)
export default defineNuxtRouteMiddleware((to, from) => {
  const userId = to.query.userId
  
  if (to.path.startsWith('/chat') && userId) {
    // 필요한 경우에만 리다이렉트
    if (to.path === '/chat' || to.path === '/chat/') {
      return navigateTo(`/chat/list?userId=${userId}`)
    }
  }
})
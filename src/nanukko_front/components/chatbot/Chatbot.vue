<template>
    <div class="w-full max-w-2xl mx-auto h-[600px] bg-white rounded-lg shadow-lg flex flex-col">
        <div class="flex-1 overflow-y-auto p-4" ref="chatContainer">
            <div v-for="(message, index) in messages" :key="index" class="mb-4 flex"
                :class="message.isUser ? 'justify-end' : 'justify-start'">
                <div class="rounded-lg px-4 py-2 max-w-[80%]"
                    :class="message.isUser ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-800'">
                    {{ message.text }}
                </div>
            </div>
            <div ref="messagesEnd"></div>
        </div>

        <div class="p-4 border-t">
            <div class="flex gap-2">
                <input v-model="inputMessage" type="text"
                    class="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:border-blue-500"
                    placeholder="메시지를 입력하세요..." :disabled="isLoading" @keyup.enter="sendMessage" />
                <button @click="sendMessage" :disabled="isLoading"
                    class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50">
                    <svg v-if="!isLoading" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
                        viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                    </svg>
                    <span v-else
                        class="h-5 w-5 block border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesEnd = ref(null)

const scrollToBottom = () => {
    nextTick(() => {
        messagesEnd.value?.scrollIntoView({ behavior: 'smooth' })
    })
}

const sendMessage = async () => {
    if (!inputMessage.value.trim()) return

    try {
        isLoading.value = true
        // 사용자 메시지 추가
        messages.value.push({ text: inputMessage.value, isUser: true })
        scrollToBottom()

        // API 호출
        const response = await fetch('/api/chatbot/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ message: inputMessage.value }),
        })

        if (!response.ok) {
            throw new Error('API 요청 실패')
        }

        const data = await response.json()

        // 봇 응답 추가
        messages.value.push({ text: data, isUser: false })
        inputMessage.value = ''

    } catch (error) {
        console.error('Error:', error)
        messages.value.push({
            text: '죄송합니다. 메시지 전송 중 오류가 발생했습니다.',
            isUser: false
        })
    } finally {
        isLoading.value = false
        scrollToBottom()
    }
}

onMounted(() => {
    scrollToBottom()
})
</script>

<style scoped>
.animate-spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}
</style>
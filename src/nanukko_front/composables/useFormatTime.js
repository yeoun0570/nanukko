// composables/useFormatTime.js

export const useFormatTime = () => {
  const formatTime = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      const date = new Date(timestamp)
      
      return new Intl.DateTimeFormat('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }).format(date)
    } catch (error) {
      console.error('시간 포맷팅 실패:', error)
      return ''
    }
  }

  const formatDate = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      const date = new Date(timestamp)
      
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }).format(date)
    } catch (error) {
      console.error('날짜 포맷팅 실패:', error)
      return ''
    }
  }

  const formatDateTime = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      const date = new Date(timestamp)
      
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }).format(date)
    } catch (error) {
      console.error('날짜/시간 포맷팅 실패:', error)
      return ''
    }
  }

  const getRelativeTime = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      const now = new Date()
      const date = new Date(timestamp)
      const diffInSeconds = Math.floor((now - date) / 1000)
      
      if (diffInSeconds < 60) return '방금 전'
      if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`
      if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전`
      if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}일 전`
      
      return formatDate(timestamp)
    } catch (error) {
      console.error('상대 시간 계산 실패:', error)
      return ''
    }
  }

  return {
    formatTime,
    formatDate,
    formatDateTime,
    getRelativeTime
  }
}
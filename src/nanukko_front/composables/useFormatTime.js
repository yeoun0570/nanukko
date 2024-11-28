// // composables/useFormatTime.js

// export const useFormatTime = () => {
//   const formatTime = (timestamp) => {
//     if (!timestamp) return ''
    
//     try {
//       const date = new Date(timestamp)
      
//       return new Intl.DateTimeFormat('ko-KR', {
//         hour: '2-digit',
//         minute: '2-digit',
//         hour12: false
//       }).format(date)
//     } catch (error) {
//       console.error('시간 포맷팅 실패:', error)
//       return ''
//     }
//   }

//   const formatDate = (timestamp) => {
//     if (!timestamp) return ''
    
//     try {
//       const date = new Date(timestamp)
      
//       return new Intl.DateTimeFormat('ko-KR', {
//         year: 'numeric',
//         month: 'long',
//         day: 'numeric'
//       }).format(date)
//     } catch (error) {
//       console.error('날짜 포맷팅 실패:', error)
//       return ''
//     }
//   }

//   const formatDateTime = (timestamp) => {
//     if (!timestamp) return ''
    
//     try {
//       const date = new Date(timestamp)
      
//       return new Intl.DateTimeFormat('ko-KR', {
//         year: 'numeric',
//         month: 'long',
//         day: 'numeric',
//         hour: '2-digit',
//         minute: '2-digit',
//         hour12: false
//       }).format(date)
//     } catch (error) {
//       console.error('날짜/시간 포맷팅 실패:', error)
//       return ''
//     }
//   }

//   const getRelativeTime = (timestamp) => {
//     if (!timestamp) return ''
    
//     try {
//       const now = new Date()
//       const date = new Date(timestamp)
//       const diffInSeconds = Math.floor((now - date) / 1000)
      
//       if (diffInSeconds < 60) return '방금 전'
//       if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`
//       if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전`
//       if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}일 전`
      
//       return formatDate(timestamp)
//     } catch (error) {
//       console.error('상대 시간 계산 실패:', error)
//       return ''
//     }
//   }

//   return {
//     formatTime,
//     formatDate,
//     formatDateTime,
//     getRelativeTime
//   }
// }


export const useFormatTime = () => {
  const formatTime = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      // 한글 형식 날짜 파싱
      if (typeof timestamp === 'string' && timestamp.includes('년')) {
        const matches = timestamp.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/)
        if (matches) {
          const [_, year, month, day] = matches
          const date = new Date(year, month - 1, day)
          return new Intl.DateTimeFormat('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          }).format(date)
        }
      }
      
      // 일반적인 timestamp 처리
      const date = new Date(timestamp)
      return new Intl.DateTimeFormat('ko-KR', {
        hour: '2-digit',
        minute: '2-digit', 
        hour12: false
      }).format(date)
    } catch (error) {
      console.error('시간 포맷팅 실패:', error, 'timestamp:', timestamp)
      return timestamp || ''
    }
  }
 
  const formatDate = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      // 한글 형식 날짜 파싱
      if (typeof timestamp === 'string' && timestamp.includes('년')) {
        const matches = timestamp.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/)
        if (matches) {
          const [_, year, month, day] = matches
          const date = new Date(year, month - 1, day)
          return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
          }).format(date)
        }
      }
      
      const date = new Date(timestamp)
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }).format(date)
    } catch (error) {
      console.error('날짜 포맷팅 실패:', error, 'timestamp:', timestamp)
      return timestamp || ''
    }
  }
 
  const formatDateTime = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      // 한글 형식 날짜 파싱
      if (typeof timestamp === 'string' && timestamp.includes('년')) {
        const matches = timestamp.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/)
        if (matches) {
          const [_, year, month, day] = matches
          const date = new Date(year, month - 1, day)
          return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          }).format(date)
        }
      }
      
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
      console.error('날짜/시간 포맷팅 실패:', error, 'timestamp:', timestamp)
      return timestamp || ''
    }
  }
 
  const getRelativeTime = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      // 한글 형식 날짜 파싱
      let date
      if (typeof timestamp === 'string' && timestamp.includes('년')) {
        const matches = timestamp.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/)
        if (matches) {
          const [_, year, month, day] = matches
          date = new Date(year, month - 1, day)
        }
      } else {
        date = new Date(timestamp)
      }
 
      const now = new Date()
      const diffInSeconds = Math.floor((now - date) / 1000)
      
      if (diffInSeconds < 60) return '방금 전'
      if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`
      if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전`
      if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}일 전`
      
      return formatDate(timestamp)
    } catch (error) {
      console.error('상대 시간 계산 실패:', error, 'timestamp:', timestamp)
      return timestamp || ''
    }
  }
 
  const formatDetailedTime = (timestamp) => {
    if (!timestamp) return ''
    
    try {
      // 한글 형식 날짜 파싱
      if (typeof timestamp === 'string' && timestamp.includes('년')) {
        const matches = timestamp.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/)
        if (!matches) return timestamp
        
        const [_, year, month, day] = matches
        const date = new Date(year, month - 1, day)
        const now = new Date()
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        
        // 오늘 날짜인 경우 시간만 표시
        if (date.toDateString() === today.toDateString()) {
          return new Intl.DateTimeFormat('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          }).format(date)
        }
        
        // 오늘이 아닌 경우 월/일 + 시간
        return `${month}/${day} ${new Intl.DateTimeFormat('ko-KR', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        }).format(date)}`
      }
      
      // 일반적인 timestamp 처리
      const date = new Date(timestamp)
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      
      if (date.toDateString() === today.toDateString()) {
        return new Intl.DateTimeFormat('ko-KR', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        }).format(date)
      }
      
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}/${day} ${new Intl.DateTimeFormat('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }).format(date)}`
    } catch (error) {
      console.error('상세 시간 포맷팅 실패:', error, 'timestamp:', timestamp)
      return timestamp || ''
    }
  }
 
  return {
    formatTime,
    formatDate,
    formatDateTime,
    getRelativeTime,
    formatDetailedTime
  }
 }
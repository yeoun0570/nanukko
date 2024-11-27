// composables/auth/useAuth.js

import { ref } from 'vue'
import { jwtDecode } from 'jwt-decode'

export function useAuth(){

  const isAuthenticted = ref(false) // 로그인 여부
  const userId = ref(null) // 사용자 아이디
  const nickname = ref(null) // 사용자 이름
  const token = ref(null) // 엑세스 토큰


    /**
   * JWT 토큰을 디코딩하여 사용자 정보를 추출하는 함수
   * @param {string} token - JWT 토큰
   */
    const decodeAndSetUserInfo = (accessToken) => {
      try{
        const decode = jwtDecode(accessToken)
  
        //JWT 페이로드에서 정보 추출
        userId.value = decode.userId
        nickname.value = decode.nickname
  
      }catch(error){
        console.error('토큰 디코딩 실패 : ', error)
        clearAuth()
      }
    }

  /**
 * 토큰을 저장하고 인증 상태를 업데이트하는 함수
 * @param {string} accessToken - JWT 액세스 토큰
 */


 // 로그인 성공시 서버에서 받은 토큰을 저장, 브라우저의 로컬 스토리지에 토큰 영구 저장
 const setToken = (accessToken) => {

  if(!accessToken) return

  // localStorage에 토큰 저장
  localStorage.setItem('access_token', accessToken)
  token.value = accessToken
  isAuthenticted.value = true //로그인 상태로 업데이트

  //토큰에서 사용자 정보 추출
  decodeAndSetUserInfo(accessToken)
  
}


  /***
   * localStrage에서 토큰 가져오는 함수
   * @returns {string | null} - 저장된 토큰 또는 null
   */
  const getToken = ()=> {
    //이미 메모리에 토큰 있으면 꺼내서 반환
    if(token.value){
      return token.value
    }

    // 없으면 localStoarage에서 가져옴
    const storedToken = localStorage.getItem('access_token')
    if(storedToken){// 꺼내올 값이 있다면
      token.value = storedToken
      isAuthenticted.value = true //로그인 상태로 업데이트

      // 저장된 토큰에서 사용자 정보 추출
      decodeAndSetUserInfo(storedToken)
    }
    return storedToken
  }

  /**
     * 모든 인증 정보를 초기화하는 함수
     */
  const clearAuth = () => {
    //localStorage에서 토큰 제거
    localStorage.removeItem('access_token')

    //상태 초기화
    token.value = null
    userId.value = null
    nickname.value = null
    isAuthenticted.value = null
  }



  /**
     * 로그아웃 처리 함수
     * - 서버에 로그아웃 요청
     * - 로컬 상태 초기화
     */
    const logout = async () => {
      try{
        //서버에 로그아웃 요청
        const response = await fetch(
          '/api/logout',//BaseURL 축가해주기
          {
            method: 'POST',
            credentials: 'include'// 쿠키 포함해서 요청
          }
        )

        if(!response.ok){
          throw new Error('로그아웃 처리중 오류가 발생했습니다.')
        }

        //로그아웃 백엔드 요청 후 로컬 상태 초기화
        clearAuth()

      }catch(error){
        console.error('로그아웃 에러:', error)
        throw error
      }
    }

    return {
      isAuthenticted,
      userId,
      nickname,
      token,

      setToken,
      getToken,
      logout,
      clearAuth
    }

}
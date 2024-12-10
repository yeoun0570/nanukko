![header](https://capsule-render.vercel.app/api?type=wave&color=gradient&height=300&section=header&text=나누꼬%20프로젝트&fontSize=40)

## 📌 프로젝트 소개
<img src="[프로젝트로고](https://github.com/yeoun0570/nanukko/issues/47#issue-2728715750)" width="50" height="50"/> 안녕하세요, 저희는 중고 거래 플랫폼 나누꼬입니다.
유아용품 중고거래 플랫폼으로, 자녀의 연령대별로 필요한 물품을 안전하게 거래할 수 있는 서비스를 제공합니다.

## 🛠 기술 스택
### Frontend
- Nuxt.js 3
- Vue.js
- Vuetify
- SockJS
- STOMP
- Pinia (상태 관리)

### Backend
- Spring Boot
- Spring Security
- JPA/Hibernate
- WebSocket
- SSE
- JWT Authentication
- MySQL

### Infrastructure
- Naver Cloud Platform
- Docker
- Nginx
- Let's Encrypt (SSL)

### DevOps & Tools
- Git/GitHub
- IntelliJ IDEA
- VSCode
- Notion

## 🚀 주요 기능

### 👥 사용자 관리
- 회원가입 및 로그인
- 프로필 관리
- 자녀 정보 등록/관리

### 🧡 사용자 맞춤 추천
- 사용자 맞춤 상품 제시

### 💰 거래 시스템
- 상품 등록/수정/삭제
- 연령대별 카테고리 분류
- 결제 시스템 연동 (토스페이먼츠)
- 에스크로 서비스

### 💬 채팅 시스템
- 실시간 1:1 채팅
- 채팅방 관리
- 이미지 전송 기능

### 🔔 알림 시스템
- 실시간 알림 (SSE)
- 거래/배송/채팅 알림
- 알림 이력 관리

### 📦 배송 관리
- 운송장 등록/조회
- 배송 상태 추적
- 배송 알림 서비스

### ⭐ 기타 기능
- 찜하기
- 리뷰 시스템

## 🔧 프로젝트 구조
```
src/
├── main/
│   ├── java/
│   │   └── nanukko/
│   │       └── nanukko_back/
│   │           ├── config/          # 각종 설정
│   │           ├── controller/      # API 컨트롤러
│   │           ├── domain/          # 도메인 모델
│   │           │   ├── blacklist/
│   │           │   ├── chat/
│   │           │   ├── jwt/
│   │           │   ├── notification/
│   │           │   ├── order/
│   │           │   ├── product/
│   │           │   │   └── category/
│   │           │   ├── report/
│   │           │   ├── review/
│   │           │   ├── user/
│   │           │   └── wishlist/
│   │           ├── dto/            # Data Transfer Objects
│   │           │   ├── blacklist/
│   │           │   ├── chat/
│   │           │   ├── file/
│   │           │   ├── jwt/
│   │           │   ├── notification/
│   │           │   ├── order/
│   │           │   ├── product/
│   │           │   ├── report/
│   │           │   ├── review/
│   │           │   ├── user/
│   │           │   └── wishlist/
│   │           ├── exception/      # 예외 처리
│   │           ├── jwt/           # JWT 관련 
│   │           ├── repository/    # 데이터 접근 계층
│   │           │   └── notification/
│   │           └── service/       # 비즈니스 로직
│   └── resources/
│       ├── mapper/               # MyBatis 매퍼
│       ├── static/              # 정적 파일
│       └── templates/           # 템플릿 파일
└── test/                        # 테스트 코드
    └── java/
        └── nanukko/
            └── nanukko_back/

## Frontend (Nuxt.js)
nanukko_front/
├── .nuxt/                  # Nuxt 빌드 파일
├── assets/                 # 정적 자원
│   ├── images/
│   ├── notification/
│   └── styles/
├── components/            # 컴포넌트
│   ├── auth/
│   ├── chat/
│   ├── common/
│   ├── notification/
│   ├── orders/
│   ├── payments/
│   ├── product/
│   └── user/
├── composables/         # 재사용 가능한 로직
│   └── auth/
├── layouts/             # 레이아웃
├── middleware/          # 미들웨어
├── pages/               # 페이지
│   ├── auth/
│   ├── chat/
│   ├── my-store/
│   ├── notice/
│   ├── orders/
│   ├── product/
│   └── user/
├── plugins/           # 플러그인
├── public/            # 공개 파일
├── store/             # 상태 관리
└── types/             # TypeScript 타입

## 주요 설정 파일 위치
프로젝트 루트/
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/main/resources/
│       └── application.properties
│
└── frontend/
├── .env
├── nuxt.config.ts
├── package.json
└── tsconfig.json
```

## 📱 실행 화면
[주요 실행 화면 스크린샷 추가]

## 🔍 주요 기술적 도전 과제
1. **상품 사용자 맞춤 추천**
   - 사용자 그룹화를 통한 로그 테이블 추적

2. **실시간 채팅/알림 구현**
   - WebSocket과 SSE를 활용한 실시간 통신
   - 클라이언트-서버 간 연결 상태 관리
   - 메시지 신뢰성 보장

3. **결제 시스템 연동**
   - 토스페이먼츠 API 연동
   - 에스크로 서비스 구현
   - 안전한 거래 보장

4. **보안 강화**
   - JWT 기반 인증
   - Spring Security 설정
   - HTTPS 적용

## 🚀 시작하기

### 프론트엔드
```bash
# 프로젝트 클론
git clone [repository-url]

# 의존성 설치
cd src/nanukko_front
npm install
npm run build

# 개발 서버 실행
npm run dev

# 배포 서버 실행
node .output/server/index.mjs
```

### 백엔드
```bash
# 프로젝트 빌드
cd 
chmod +x gradlew
./gradlew clean build -x test

# 개발 서버 실행
./gradlew bootrun

# 배포 서버 실행
java -jar build/libs/[project-name].jar
```

## 👥 팀원
- [(팀장)윤여운] - 서버관리, (백/프론트)마이페이지, (백/프론트)알림/메일, (백/프론트)토스결제, (백/프론트)배송추적, (백/프론트)챗봇, (백)이미지 업로드
- [(팀원)이효승] - 기획, 프론트엔드 공통 UI/UX, 산출물 관리
- [(팀원)신희원] - ERD, (백/프론트)채팅, (백/프론트)회원가입/로그인, 프론트엔드 구조
- [(팀원)김은강] - 노션관리, (백/프론트)상품관리, 산출물 관리, (백/프론트)챗봇

## 📜 트러블슈팅

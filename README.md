# 🌍 ThinkTrip 백엔드

AI 기반 여행 일정 추천 서비스 **ThinkTrip**의 Spring Boot 백엔드 프로젝트입니다.

---

## ✅ 기술 스택

- **Java 17**
- **Spring Boot 3.4.4**
- **Gradle**
- **MySQL 8 (AWS RDS)**
- **Docker / Docker Hub**
- **Amazon EC2 (Ubuntu 22.04)**

---

## 📁 프로젝트 구조

```
thinktrip-api/
├── src/
│   └── main/
│       ├── java/
│       │   └── com.thinktrip.thinktrip_api/
│       │       ├── config/               # Spring Security, JWT 설정
│       │       ├── controller/           # API 컨트롤러 (User, Diary, TravelPlan)
│       │       │   ├── DiaryController.java
│       │       │   ├── TravelPlanController.java
│       │       │   └── UserController.java
│       │       ├── domain/               # JPA 엔티티 및 레포지토리
│       │       │   ├── diary/
│       │       │   ├── travel/
│       │       │   └── user/
│       │       ├── dto/                  # 요청/응답 DTO 클래스
│       │       │   ├── diary/
│       │       │   ├── travel/
│       │       │   └── user/
│       │       ├── exception/            # 글로벌 예외 처리
│       │       ├── jwt/                  # JWT 관련 클래스 (필터, 토큰 유틸 등)
│       │       ├── service/              # 비즈니스 로직
│       │       │   ├── diary/
│       │       │   ├── travel/
│       │       │   └── user/
│       │       └── ThinktripApiApplication.java
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           └── application-prod.yml
├── build.gradle
├── Dockerfile
└── README.md
```

---

## 🧪 개발 환경 (로컬)

- **프로파일**: `dev`
- **DB 연결**: 로컬 MySQL (`localhost:3306`)
- **접속 정보**:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/thinktripdb
    username: dev
    password: devpass
```

---

## ☁️ 운영 환경 (배포)

- **프로파일**: `prod`
- **DB 연결**: AWS RDS MySQL
- **서버**: EC2 Ubuntu + Docker

---

## 🔐 GitHub Secrets 구성

CI/CD 및 OAuth2 인증을 위해 다음과 같은 GitHub Secrets가 설정되어야 합니다.

| 키 | 설명 |
|----|------|
| `EC2_HOST` | EC2 퍼블릭 IP 주소 (ex. `3.39.xxx.xxx`) |
| `EC2_KEY` | EC2 접속용 PEM 키 (Base64 인코딩된 개인 키) |
| `DOCKER_USER` | Docker Hub 사용자 이름 |
| `DOCKER_PASS` | Docker Hub 비밀번호 또는 액세스 토큰 |
| `KAKAO_CLIENT_ID` | Kakao Developers에서 발급받은 Client ID |
| `KAKAO_CLIENT_SECRET` | Kakao Developers에서 발급받은 Client Secret |
| `JWT_SECRET` | JWT 토큰 서명을 위한 비밀 키 |
| `FRONTEND_REDIRECT_URL` | 소셜 로그인 성공 후 리디렉션할 프론트엔드 주소 (예: `https://thinktrip.kr/oauth2/success`) |

### ✅ 적용 방식

- GitHub Actions 워크플로우 내에서 `application-prod.yml` 혹은 환경 변수에 주입
- 또는 Docker 실행 시 `-e` 옵션을 통해 전달

```bash
docker pull ${{ secrets.DOCKER_USER }}/thinktrip-app:latest
          docker rm -f thinktrip-app || true
          docker run -d \
            -v /home/ubuntu/thinktrip/profile-images:/app/uploads \
            -p 8080:8080 \
            -e SPRING_PROFILES_ACTIVE=prod \
            -e KAKAO_CLIENT_ID=${{ secrets.KAKAO_CLIENT_ID }} \
            --name thinktrip-app \
            ${{ secrets.DOCKER_USER }}/thinktrip-app:latest
```
---

## 🚀 배포 플로우

1. Spring 프로젝트 `./gradlew build`
2. Docker 이미지 빌드 → Docker Hub push
3. EC2에서 pull + 실행
4. 프로파일: `prod`

---

## 📌 Git 브랜치 전략

| 브랜치         | 설명                          |
|-------------|-----------------------------|
| `master`    | 운영/배포용 브랜치 (commit시 ci/cd)  |
| `dev`       | 개발용 기능 통합 브랜치               |
| `feature/~` | 기능별 브랜치 분기 후 dev에서 통합 및 테스트 |

---

## ⚙️ CI/CD 구성

- **GitHub Actions** 기반 자동 배포 구성
- `master` 브랜치에 push 또는 merge 시 자동 실행
- 워크플로우 내용:

```yaml
on:
  push:
    branches: [ master ]

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
    - Checkout 코드
    - Gradle로 빌드
    - Docker 이미지 빌드 및 Docker Hub에 push
    - EC2에 SSH 접속 → 최신 이미지 pull → 컨테이너 재실행
```

- AWS EC2에는 Docker가 설치되어 있으며,  
  GitHub Secret을 통해 다음 정보들이 보호된 상태로 전달됨:
  - EC2 접속용 Private Key (`EC2_KEY`)
  - Docker Hub 계정 정보 (`DOCKER_USER`, `DOCKER_PASS`)

---

# 📘 ThinkTrip API 명세

## 🔐 사용자(User) API

| Method | Endpoint                        | 설명                               |
|--------|----------------------------------|------------------------------------|
| POST   | `/api/users/signup`             | 자체 회원가입                      |
| POST   | `/api/users/login`              | 로그인 (JWT 토큰 발급)             |
| GET    | `/api/users/me`                 | 내 프로필 조회                     |
| PATCH  | `/api/users/me`                 | 내 정보 수정 (이메일/비번 등)     |
| DELETE | `/api/users/me`                 | 회원 탈퇴 (계획 및 다이어리 포함 삭제) |
| POST   | `/api/users/profile-image`      | 프로필 이미지 업로드               |
| DELETE | `/api/users/profile-image`      | 프로필 이미지 삭제                 |
| GET    | `/api/users/profile-image`      | 내 프로필 이미지 조회              |
| GET    | `/api/users/profile-image/{id}` | 특정 유저 프로필 이미지 조회       |

---

## 🗓️ 여행 계획(Travel Plan) API

| Method | Endpoint                              | 설명                            |
|--------|----------------------------------------|---------------------------------|
| POST   | `/api/travel-plans/user`              | 직접 입력 여행 계획 저장        |
| POST   | `/api/travel-plans/generated`         | GPT 생성 여행 계획 저장         |
| GET    | `/api/travel-plans/user`              | 직접 생성한 여행 계획 목록 조회 |
| GET    | `/api/travel-plans/generated`         | GPT 생성 여행 계획 목록 조회    |
| GET    | `/api/travel-plans/{planId}`          | 여행 계획 상세 조회             |
| PUT    | `/api/travel-plans/{planId}`          | 여행 계획 수정                  |
| DELETE | `/api/travel-plans/{planId}`          | 여행 계획 삭제                  |
| GET    | `/api/travel-plans/dday`              | 가장 빠른 여행 일정 D-Day 조회 |

---

## 📓 다이어리(Diary) API

| Method | Endpoint                            | 설명                                 |
|--------|-------------------------------------|--------------------------------------|
| POST   | `/api/diaries`                      | 다이어리 작성 (이미지 첨부 가능)    |
| GET    | `/api/diaries`                      | 나의 모든 다이어리 조회             |
| GET    | `/api/diaries/{diaryId}`            | 다이어리 상세 조회                  |
| PUT    | `/api/diaries/{diaryId}`            | 다이어리 수정 (이미지 포함)         |
| DELETE | `/api/diaries/{diaryId}`            | 다이어리 삭제 (이미지도 함께 삭제됨)|
| POST   | `/api/diaries/{diaryId}/images`     | 다이어리에 이미지 별도 업로드       |

---

## 🤖 GPT API

| Method | Endpoint                                | 설명                          |
|--------|-------------------------------------|-------------------------------|
| GET    | `/thinktrip/users/gpt/usage`        | 사용자의 GPT 사용 내역 조회   |
| POST   | `/thinktrip/users/gpt/usage`        | GPT 사용 요청 기록 저장       |

---

## 🖼️ 정적 이미지 접근 경로

| 경로                                  | 설명                          |
|---------------------------------------|-------------------------------|
| `/uploads/profile/{userId}.jpg`       | 유저 프로필 이미지            |
| `/uploads/diary/{filename}`           | 다이어리 이미지               |

---

## 🚨 예외 응답 포맷

| 상태 코드 | 설명             | 예시 응답 메시지 |
|-----------|------------------|------------------|
| `400`     | 잘못된 요청      | `{ "error": "필수 항목 누락" }` |
| `401`     | 인증 필요        | `{ "error": "로그인이 필요합니다." }` |
| `403`     | 접근 권한 없음   | `{ "error": "접근 권한이 없습니다." }` |
| `404`     | 경로 존재하지 않음 | `{ "error": "요청하신 API 엔드포인트가 존재하지 않습니다." }` |

---

## 📌 핵심 기능 요약 
- JWT 인증 기반 사용자 인증 시스템 구축

- 소셜 로그인 연동 (Kakao OAuth2) 및 자동 회원가입 처리

- 다이어리 CRUD + 이미지 첨부 기능 (로컬/배포 환경 경로 관리 포함)

- 여행 계획 등록/수정/삭제 기능 (GPT 자동 생성 및 수동 입력 구분)

- S3-like 파일 업로드 경로 관리 및 정적 리소스 제공

- D-DAY 계산 로직 구현 (가장 빠른 일정 기준 동적 반환)

- 예외 처리 통일 및 응답 포맷 표준화 (JSON 기반)

--- 

## 🧠 개발 중 중점 둔 부분
- Spring Security와 JWT를 사용한 무상태 인증 구조

- Multipart 요청 처리 + 이미지 저장 + URL 리턴까지의 파일 처리 파이프라인

- GitHub Actions를 통한 자동화된 CI/CD 배포 파이프라인 구성

- 사용자 탈퇴 시 연관된 계획 및 다이어리 Cascade 삭제 처리

- JWT로 식별된 사용자를 기준으로만 데이터 접근 제한 (권한 검증)

---

## 💡 기술적 포인트
- @AuthenticationPrincipal을 활용한 사용자 추적 및 검증

- RESTful 설계를 따르면서도 사용자 편의성을 고려한 URL 구조 설계

- Docker + EC2 + GitHub Secrets를 활용한 보안성 있는 배포

- 이미지 URL만 응답에 포함하고, 정적 경로로 브라우저에서 직접 접근 가능하게 구성

---
### 🗂️ 프로필 이미지 저장 경로

| 환경 | 경로                    |
|------|-------------------------|
| dev  | `./uploads/`            |
| prod | `/app/uploads/` (EC2에서 Docker 볼륨 마운트됨) |

---

## 🔑 Kakao 소셜 로그인 구성

- OAuth2.0 기반 **카카오 로그인 연동**
- `Spring Security OAuth2 Client`를 사용하여 구성
- 로그인 시 카카오로부터 사용자 정보(email, nickname 등) 수신
- 사용자가 DB에 존재하지 않으면 **자동 회원가입 처리**
- 로그인 성공 시 **JWT 토큰을 발급**하고 프론트엔드로 리디렉션

### ✅ 리디렉션 흐름

1. 프론트에서 `/oauth2/authorization/kakao` 요청 → 카카오 로그인 페이지로 이동
2. 로그인 성공 시 `/login/oauth2/code/kakao`로 리디렉션됨
3. 백엔드의 `OAuth2LoginSuccessHandler`에서 JWT 생성
4. 설정된 환경변수 `FRONTEND_REDIRECT_URL`로 다음과 같이 리디렉션됨:
https://{FRONTEND_REDIRECT_URL}?token={JWT}


- 이 토큰은 이후 API 요청 시 `Authorization: Bearer <token>` 헤더에 담아 사용

---


### 🧾 API 예시

#### 회원가입

```
POST /api/users/signup
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "1234",
  "name": "홍길동",
  "nickname": "길동이",
  "address": "서울시",
  "travelStyle": "자연"
}
```

✅ 응답:

```json
{
  "message": "회원가입이 완료되었습니다."
}
```

---

#### 로그인

```
POST /api/users/login
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "1234"
}
```

✅ 응답:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

❌ 실패 응답:

```json
{
  "message": "이메일 또는 비밀번호가 일치하지 않습니다."
}
```

---

#### 사용자 정보 조회 (`/api/users/me`)

✅ 응답 예시:

```json
{
  "email": "test@example.com",
  "nickname": "길동이",
  "address": "서울시",
  "travelStyle": "자연",
  "profileImageUrl": "/api/users/profile-image"
}
```


---

### 🔒 보안 구성 요약

- `BCryptPasswordEncoder`로 비밀번호 암호화
- JWT 토큰 발급 및 검증 (`JwtAuthenticationFilter`)
- 엔드포인트 접근 제어는 `SecurityConfig`에서 관리
- 모든 응답은 JSON 형식 통일
- JWT 만료시간 설정: 기본 1시간
- 인증 실패 시 401 Unauthorized 응답
- CORS는 `localhost:3000` 등 프론트엔드 도메인 허용 설정 포함

---

## 🙌 참여자

- GitHub: [김현우](https://github.com/KHW01104)

## 🙌 작성자

- GitHub: [김정현](https://github.com/ranpia)
- Repository: [Th-nkTrip/thinktrip-back](https://github.com/Th-nkTrip/thinktrip-back)

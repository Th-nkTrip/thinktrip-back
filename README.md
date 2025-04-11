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
│       │       ├── controller/           # 사용자 API 컨트롤러
│       │       ├── domain/user/          # User 엔티티 및 Repository
│       │       ├── dto/user/             # 회원가입/로그인 요청 DTO
│       │       ├── jwt/                  # JWT 관련 클래스
│       │       ├── service/              # 비즈니스 로직 (회원가입, 로그인 등)
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

### Docker 실행 예시

```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /home/ubuntu/thinktrip/profile-images:/app/uploads \
  --name thinktrip-app \
  ranpia/thinktrip-app:latest
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

## 📋 TODO

- 로그인 기능 구현 (+카카오 소셜 로그인...?)
- 마이페이지 기능
- 프리미엄 구독 확인
- JWT 리프레시 토큰

---

## ✅ 기능별 정리

### 🔐 사용자 인증 기능

| 기능       | 설명 |
|------------|------|
| 회원가입   | 이메일, 비밀번호, 닉네임, 주소 등 사용자 정보 저장 |
| 로그인     | 이메일 & 비밀번호 확인 후 JWT 토큰 발급 |
| 비밀번호 암호화 | BCryptPasswordEncoder 사용 |
| 토큰 인증 | 요청 시 Authorization 헤더를 통한 사용자 인증 |
| 보안 필터 | JwtAuthenticationFilter + Spring Security 적용

---

### 🖼️ 프로필 이미지 기능

| 메서드 | 엔드포인트                     | 설명                                      |
|--------|--------------------------------|-------------------------------------------|
| GET    | `/api/users/me`               | 사용자 전체 정보 조회 (이미지 URL 포함)     |
| POST   | `/api/users/profile-image`    | 프로필 이미지 업로드 (multipart/form-data) |
| DELETE | `/api/users/profile-image`    | 프로필 이미지 삭제 → 기본 이미지로 초기화   |
| GET    | `/image/{email}`              | 업로드된 이미지 조회                       |
| GET    | `/image/default.jpg`          | 기본 이미지 제공                           |

#### 📁 저장 경로

| 환경 | 경로 |
|------|------|
| dev  | `./uploads/` |
| prod | `/app/uploads/` (EC2 경로 마운트됨)

- `/api/users/me` 응답에 포함된 `profileImageUrl`을 `<img src="...">`로 사용 가능
- 이미지가 없으면 자동으로 `/image/default.jpg`가 반환됨

---

### 📌 API 설명

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

#### 인증 API 호출 예시

```
GET /api/test/secure
Authorization: Bearer {JWT_TOKEN}
```

✅ 응답:

```json
{
  "message": "인증된 사용자: test@example.com"
}
```

---

### 🔒 보안 구성 요약

- `BCryptPasswordEncoder`로 비밀번호 암호화
- `JwtAuthenticationFilter`로 토큰 검증
- `SecurityConfig`에서 엔드포인트 접근 제어
- 모든 응답은 JSON 형식으로 통일
- `Authentication` 객체를 통해 로그인 사용자 정보 접근 가능

---

## 🙌 참여자
- GitHub: [김현우](https://github.com/KHW01104)

## 🙌 작성자
- GitHub: [김정현](https://github.com/ranpia)
- Repository: [Th-nkTrip/thinktrip-back](https://github.com/Th-nkTrip/thinktrip-back)
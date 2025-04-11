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

## ✅ 기능별 정리

### 🔐 사용자 인증 및 프로필 관련 API

| 메서드 | 엔드포인트                         | 설명                                             |
|--------|------------------------------------|--------------------------------------------------|
| POST   | `/api/users/signup`                | 회원가입                                         |
| POST   | `/api/users/login`                 | 로그인 후 JWT 토큰 발급                          |
| GET    | `/api/users/me`                    | JWT 토큰을 통해 인증된 사용자 정보 조회          |
| POST   | `/api/users/profile-image`         | 프로필 이미지 업로드 (multipart/form-data)       |
| GET    | `/api/users/profile-image`         | 본인 프로필 이미지 조회 (Content-Type 포함)      |
| GET    | `/api/users/profile-image/{id}`    | 다른 사용자 프로필 이미지 조회 (Content-Type 포함) |
| DELETE | `/api/users/profile-image`         | 프로필 이미지 삭제 → 기본 이미지로 초기화        |
| GET    | `/api/test/secure`                 | 인증된 사용자 테스트 응답                         |

- JWT는 `Authorization: Bearer <token>` 형식으로 전달
- 비밀번호는 `BCryptPasswordEncoder`로 암호화
- 인증된 사용자 정보는 `Authentication` 또는 `@AuthenticationPrincipal`을 통해 접근
- 이미지 응답은 `Resource` 형태로 반환되며, `Content-Type: image/jpeg` 또는 `image/png` 포함됨

---

### 🗂️ 프로필 이미지 저장 경로

| 환경 | 경로                    |
|------|-------------------------|
| dev  | `./uploads/`            |
| prod | `/app/uploads/` (EC2에서 Docker 볼륨 마운트됨) |

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

#### 공통 오류 응답 예시

```json
{
  "error": "파일 저장 중 오류가 발생했습니다."
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

### 📌 기타

- Swagger UI를 통해 API 테스트 자동화 예정 (미구현)
- Refresh Token 기능은 향후 도입 예정

---

## 🙌 참여자

- GitHub: [김현우](https://github.com/KHW01104)

## 🙌 작성자

- GitHub: [김정현](https://github.com/ranpia)
- Repository: [Th-nkTrip/thinktrip-back](https://github.com/Th-nkTrip/thinktrip-back)
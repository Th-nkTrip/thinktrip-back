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
│       │   └── com.thinktrip...
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

```
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

| 브랜치 | 설명 |
|--------|------|
| `master` | 운영/배포용 브랜치 (commit시 ci/cd) |
| `dev`  | 개발용 기능 통합 브랜치 |
| `etc`  | 기능별 브랜치 분기후 dev에서 통합 및 테스트 |

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

- 로그인 기능 구현(+카카오 소셜 로그인...?)

---

## 🙌 참여자
- GitHub: 김현우(github링크)

## 🙌 작성자 
- GitHub: [김정현](https://github.com/ranpia)
- Repository: [Th-nkTrip/thinktrip-back](https://github.com/Th-nkTrip/thinktrip-back)

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

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/thinktripdb
    username: dev
    password: devpass

---

## ☁️ 운영 환경 (배포)

- **프로파일**: `prod`
- **DB 연결**: AWS RDS MySQL
- **서버**: EC2 Ubuntu + Docker

### Docker 실행 예시

docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name thinktrip-app \
  ranpia/thinktrip-app:latest

---

## 🚀 배포 플로우

1. Spring 프로젝트 `./gradlew build`
2. Docker 이미지 빌드 → Docker Hub push
3. EC2에서 pull + 실행
4. 프로파일: `prod`

---

## 📌 Git 브랜치 전략

| 브랜치 | 설명                     |
|--------|--------------------------|
| `main` | 운영/배포용 브랜치 (CI/CD 연결 예정) |
| `dev`  | 개발용 기능 통합 브랜치            |

---

## 📋 TODO

- 

---

## 🙌 작성자 

- GitHub: [ranpia](https://github.com/ranpia)
- Repository: [Th-nkTrip/thinktrip-back](https://github.com/Th-nkTrip/thinktrip-back)

---
## 🙌 참여자
- GitHub: 김현우(github링크)

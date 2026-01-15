# alba

알바 앱 프로젝트

---

## Tech Stack

* Android
* Java (Spring Boot)
* MariaDB

---

## Database

* MariaDB 11.x
* HikariCP (Spring Boot 기본 커넥션 풀)

---

## Backend

* Spring Boot 3.4.5
* JPA (Hibernate)

---

## Server

* 로컬 테스트 이후 AWS EC2 Free Tier 환경 배포

---

## Authentication & Security

본 프로젝트는 외부 인증 및 SNS 로그인 연동을 전제로 설계된 구조,
아래 인증 방식들이 단계적으로 적용될 예정입니다.

### 적용 / 예정 인증 방식

* JWT 기반 인증 (Access / Refresh Token)
* OAuth2 연동

    * 네이버 로그인
    * Google 로그인
    * 기타 SNS 로그인 확장 가능 구조

모든 시크릿 키 및 인증 정보는 Git에 포함되지 않아야 합니다.

---

## Git Ignore 대상

### Git에 포함되면 안 되는 항목

### Backend (Spring Boot)

* application-local.yml
* application-prod.yml
* OAuth Client ID / Client Secret
* 네이버 Client Secret
* Google Client Secret
* JWT Secret Key
* DB 접속 정보 (URL / Username / Password)
* .env

### Frontend (Android)

* local.properties
* API Key / Secret Key
* OAuth 관련 키 값
* Android 서명 키 (*.jks)

인증 관련 정보는 환경 변수 또는 로컬 설정 파일로만 관리합니다.

---

## 인증 정보 관리 원칙

* OAuth / JWT 시크릿은 코드에 직접 작성하지 않음
* application-*.yml 또는 환경 변수로 분리

---

## 프로젝트 의의 및 목적

본 프로젝트는 미완성 상태의 알바 앱을 리빌딩하며,
아래 기능들을 직접 설계/연동/배포하는 것을 목표로 합니다.

### 핵심 목표

* 네이버 지도 API 재연계 및 위치 기반 기능 구현
* OAuth2 기반 SNS 로그인 연동

    * 네이버
    * Google
    * 기타 SNS 확장
* Android ↔ Spring Boot ↔ MariaDB 구조 이해 및 적용
* 인증 및 보안 설계
* 
실제 서비스에 가까운 인증·지도·연동·배포 구조를 목표로 한 프로젝트입니다.

---
## Note

현재는 기능 리빌딩 및 구조 정리 단계,
인증 및 외부 API 연동을 중심으로 확장을 진행 중입니다.

--

1/15
로컬기준
프로젝트 폴더명 : alba_training\gradle\wrapper
 로 변경
distributionUrl=https\://services.gradle.org/distributions/gradle-8.14-bin.zip

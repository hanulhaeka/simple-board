# README_UPDATE

# README

# 1. 프로젝트 소개

- Spring, Spring Boot, JPA 등을 학습한 후 실습을 위해 진행하는 프로젝트
- CRUD 기능을 기반으로 한 게시판에 Spring Security를 적용하여 인증/인가 기능 추가

# 2. 기술 스택

### Backend

- Spring Boot
- Spring MVC
- JPA / Spring Data JPA
- Spring Security

### Frontend (Server-Side Rendering)

- Thymeleaf
- Bootstrap

### Database

- MySQL

### Test

- JUint (JUnit5)

# 3. 주요 기능

## 회원 가입

![image.png](images/image.png)

- 사용자는 아이디와 비밀번호를 입력하여 회원 가입할 수 있다.
- 비밀번호는 BCrypt 방식으로 암호화하여 데이터베이스에 저장한다.

## 로그인

![image.png](images/image%201.png)

- 사용자가 아이디와 비밀번호를 입력하여 로그인할 수 있다.
- Spring Security의 Form Login 기반 인증 방식 사용
- 로그인 성공 시 인증이 필요한 기능에 접근 가능, 로그인 하지 않고 인증이 필요한 기능에 접근 시 로그인 화면으로 리다이렉트 된다.

## 로그아웃

![image.png](images/image%202.png)

- 게시글 목록 화면에 로그아웃 버튼 누를 시 세션 정보를 삭제하여 로그아웃 진행

## 게시글 작성

![image.png](images/image%203.png)

- 로그인 후 게시글 목록 화면에 게시글 작성 버튼이 보이며, 해당 버튼을 눌러 게시글 작성 페이지로 이동
- 게시글 작성 페이지는 로그인 후 접근 가능하며, 비로그인 사용자는 로그인 화면으로 리다이렉트
- 게시글 작성 페이지에서 제목과 본문을 작성하고 저장 버튼을 눌러 게시글 생성

## 게시글 조회

![image.png](images/image%202.png)

- 홈 화면이자 게시글 목록 화면에서 원하는 게시글 클릭

![image.png](images/image%204.png)

- 원하는 게시글 클릭 시 해당 게시글 상세 내역 페이지로 이동
- 게시글 리스트와 게시글 상세 내역 조회는 비로그인 사용자 포함 모든 사용자가 접근 가능

## 게시글 수정

![image.png](images/image%205.png)

- 게시글 상세 내역 화면에서 수정 버튼을 눌러 게시글 수정 페이지로 이동
- 본인 글이거나 사용자가 관리자 일 경우에만 수정 버튼 표시

![image.png](images/image%206.png)

- 게시글 수정 페이지로 이동

![image.png](images/image%2012.png)

- 게시글 수정 페이지에서 게시글 수정 후 수정 버튼을 누른다.

![image.png](images/image%207.png)

- 게시글 수정 버튼을 누른 후 해당 게시글의 상세 내역 페이지로 이동된다.
- 본인이 작성한 글이 아니거나 관리자가 아닐 경우 게시글 수정 불가 (서비스 계층 예외 발생)

## 게시글 삭제

![image.png](images/image%208.png)

![image.png](images/image%209.png)

- 삭제를 원하는 게시글읭 상세 내역 페이지로 이동
- 삭제 버튼을 눌러 게시글을 삭제
- 본인 글이거나 관리자가 아닐 경우 삭제 버튼이 표시되지 않는다.

![image.png](images/image%2010.png)

- 본인이 작성한 글이 아니거나 관리자가 아닐 경우 게시글 삭제 불가 (서비스 계층 예외 발생)

# 4. 실행 방법

### 1. 프로젝트 클론

```jsx
git clone https://github.com/seungjig/simple-board.git
cd simple-board
```

### 2. 자바 & 빌드 도구 버전 확인

- Java 21
- Spring Boot 3.5
- Gradle 8.14

### 3. 데이터베이스 설정

```jsx

spring.datasource.url=jdbc:mysql://localhost:3306/board?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=<yourpassword>

spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

> **spring.jpa.hibernate.ddl-auto=create 설정으로 인해 실행 시마다 스키마를 새로 생성하니 주의**

### 4. 프로젝트 실행

**Gradle**

```jsx
./gradlew bootRun
```

### 5. 브라우저에서 접속

```jsx
http://localhost:8080/article/list
```

### 6. 회원가입과 로그인

- 회원 가입 후 로그인하여 게시글 작성/수정/삭제 기능을 사용할 수 있다.

```jsx
로그인 페이지: /auth/signin
회원가입 페이지: /auth/signup
```

# 5. 프로젝트 구조

### 패키지 구조

```jsx
src
└── main
    ├── java
    │   └── com.example.simple_board
    │       ├── config
    │       │     └── JpaAuditingConfig          # JPA Auditing 설정
    |       |     └── SecuritConfig              # Spring Security 설정
    │       │
    │       ├── exception
    │       │     ├── DuplicateUserIdException    # 회원 가입 시 동일한 유저 아이디
    │       │     └── NotHaveAuthorityException   # 수정/삭제 요청 시 권한 없음
    │       │
    │       ├── model
    │       │     ├── dto
    │       │     │     ├── ArticleDto           # 게시글 요청/응답 DTO
    │       │     │     └── SignupDto            # 회원가입 DTO
    │       │     └── Article                    # 게시글 Entity
    |       |     └── Role                       # 권한
    |       |     └── User                       # 사용자
    │       │
    │       ├── repository
    │       │     └── ArticleRepository          # Spring data jpa 사용
    |       |     └── UserRepository
    |       ├── Security
    |       |      └── CustomeUserDetails
    |       |      └── CustomeUserDetailsService
    |       |      └── SecurityContextUserProvider # 요청 유저 정보 반환
    │       │
    │       ├── service
    │       │     └── ArticleService             # 게시글 비즈니스 로직 처리
    |       |     └── AuthService                # 회원가입과 로그인 비즈니스 로직 처리
    │       │
    │       └── web
    │             └── ArticleController          # 게시글 Controller
    |             └── AuthController             # 회원가입과 로그인 처리 Controller
    │
    └── resources
        ├── templates                            # thymeleaf 탬플릿
        |     ├──article
        |     |     └── article-add.html         # 게시글
        |     |     └── article-content.html
        |     |     └── article-list.html
        |     |     └── article-update.html
        │     └──auth
        │          └── auth-signin.html          # 회원가입과 로그인
        │          └── auth-signup.html
        │
        └── application.properties
```

# 6. 인증 / 인가

## 인증 인가 정책

**비회원**

- 회원 가입, 로그인, 게시글 조회 리소스에만 접근 가능
- 게시글 리스트와 게시글 상세 내역 조회만 가능

**회원**

- 모든 리소스에 접근 가능 (URL 접근 제한)
- 게시글 수정과 삭제는 본인 글 일 경우에만 가능 (메서드 레벨 인가)
- 본인 글 일 경우에만 해당 게시글 상세 내역에 수정과 삭제 버튼 표시

**관리자**

- 모든 리소스에 접근 가능 (URL 접근 제한)
- 모든 게시글에 대해 수정과 삭제 가능 (메서드 레벨 인가)
- 모든 게시글에 수정과 삭제 버튼 표시

## 구현 방식

- 해당 프로젝트는 CRUD 기능만 있는 게시판을 먼저 만들었다.
- 그 후 feature/auth-raw 브랜치와 feature/auth-security 브랜치를 만들어서 동일한 인증/인가 기능을 각각 Java/Spring 기반과 Spring Security 기반으로 만들었다.
- 최종적으로 Spring Security 기반으로 만든 브랜치를 main 브랜치와 병합하였다.

[세션 기반 인증/인가 기능 구현 요약 문서](docs/auth/raw_auth_summary.md)

[Spring Security 기반 인증/인가 기능 구현 요약 무선](docs/auth/security_auth_summary.md)

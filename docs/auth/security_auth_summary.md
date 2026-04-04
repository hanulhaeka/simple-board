# 스프링 시큐리티 인증 인가 기능 구현

# 1. 개요

기존에 세션 기반으로 구현한 인증 및 인가 기능을 스프링 시큐리티 기반으로 재구현하여 동일한 기능을 제공하도록 하였다.

---

# 2. 인증

### 로그인 처리 과정

1. 클라이언트가 로그인 페이지를 요청하면, 서버는 커스텀 로그인 페이지(auth-signin.html, Thymeleaf)를 반환한다.
2. 클라이언트는 아이디와 비밀번호를 입력한 뒤 서버로 전송한다.
3. Spring Security의 필터 체인이 해당 요청을 가로챈다.
4. AuthenticationManager는 인증 처리를 AuthenticationProvider에게 위임한다
5. AuthenticationProvider는 CustomeUserDetailsService를 통해 CustomUserDetails를 조회한다.
6. 조회된 CustomUserDetails를 기반으로 인증이 완료된  Authentication 객체를 생성한다.
7. 생성된 Authentication 객체를 SecurityContext에 저장한다.
8. 인증이 완료되면 홈 화면(/)으로 리다이렉트된다.

### 유저 정보 조회

**CustomUserDetails**

```java
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String userId;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long id, String userId, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userId;
    }

}
```

- 인증 이후 User 객체의 기본키 값을 활용하기 위해 해당 객체의 기본키 값을 추가적으로 CustomUserDetails에 저장하도록 했다

**CustomUserDetailsService**

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        return new CustomUserDetails(
                user.getId(),
                user.getUserId(),
                user.getHashedPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

- 로그인 시 입력된 userId를 기준으로 UserRepository에서  User 객체를 조회하고, 조회 결과를 활용해 CustomUserDetails를 생성해 반환한다.

### 비밀번호 검증

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

- PasswordEncoder로 BCryptPasswordEncoder를 스프링 빈으로 등록한다.
- 회원 가입 시 해당 스프링 빈을 이용해 비밀번호의 해시 값을 데이터베이스에 저장한다.
- 로그인 시 Spring Security가 내부적으로 스프링 빈으로 등록된 PasswordEncoder를 사용하여 전송된 비밀번호와 데이터베이스에 저장된 비밀번호의 해시 값을 비교한다.

### 인증 성공 후 세션 유지

**로그인 시**

- 인증 완료된 Authentication 객체를 SecurityContext에 저장한다.
- SecurityContext를 HttpSession에 저장한다.

**인증 이후 유저 정보 흐름**

- 인증된 유저의 요청이 올 경우, Cookie로 전송된 Session ID를 활용해서 HttpSession에서 SecurityContext를 조회한다.
- 조회된 SecurityContext를 쓰레드로컬기반의 SecurityContextHolder에 저장한다.
- 요청 처리 과정에서 인증된 유저 정보가 필요한 경우 SecurityContextHolder를 활용해서 해당 정보를 조회한다.
- 요청이 처리된 후 SecurityContextHolder에 저장된 정보는 제거된다.

---

# 3. 인가

## 개요

- 비로그인 사용자는 게시글 읽기, 로그인, 회원가입만 가능
- 로그인 사용자는 게시글 작성이 가능
- 로그인 사용자 중 일반 유저는 본인이 작성한 게시글만 수정 및 삭제 가능
- 로그인 사용자 중 관리자는 모든 글 수정 및 삭제 가능

## 비로그인

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

		...

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/", "/auth/signin", "/article/list", "/article/content/**", "/auth/signup").permitAll()
                        .anyRequest().authenticated()
                )

								...

                );

        return http.build();
    }
}
```

- SecurityConfig 설정 클래스를 이용해서 SecurityFilterChain을 등록한다.
- 정적 리소스, 홈(/), 게시글 리스트 조회(/article/list), 특정 게시글 내용 조회(/article/content/**), 회원 가입(/auth/signup), 로그인(/auth/signin) URL은 모든 요청에 대해 허용하였다.
- 그 이외의 요청은 authenticated()로 설정하여 인증된 사용자만 접근할 수 있도록 구성하였다.
- 따라서 비로그인 사용자는 회원 가입, 로그인, 게시글 리스트 조회, 게시글 내용 조회 기능만 사용할 수 있다.

## 로그인

### URL 인가

- 게시글 작성, 삭제, 수정 URL은 SecurityFilterChain 설정을 통해 인증된 사용자만 접근할 수 있도록 설정했다.

### 메서드 인가

- 일반 유저일 경우 게시글 삭제와 수정은 본인이 작성한 글에 대해서만 가능하고, 관리자일 경우에는 모든 글에 대해서 삭제와 수정이 가능하기에 메서드 레벨 인가로 구현하였다.

**SecurityContextUserProvider**

```java
@Component
public class SecurityContextUserProvider {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

}
```

- SecurityConextHolder에서 유저의 userId를 반환하는 메서드를 가진 SecurityContextUserProvider를 따로 스프링 빈으로 등록했다.
- 서비스 계층에 순수성과 테스트 코드 작성에 편의를 위해 해당 빈을 등록했다.

**서비스 계층 코드**

```java
public User getCurrentUser() {
        String userId = securityContextUserProvider.getCurrentUserId();
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 사용자를 찾을 수 없습니다"));
    }
```

- 서비스 계층에 SecurityContextUserProvider를 이용해 user 객체를 반환하는 메서드를 작성하여 메서드 인가를 위해 활용했다.

#### **게시글 작성**

**컨트롤러 계층 코드**

```java
		// 게시글 저장
    @PostMapping(value = "/article/add")
    public String addArticleForm(@Validated @ModelAttribute("articleDto") ArticleDto articleDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "article/article-add";
        }

        ArticleDto savedArticleDto = articleService.create(articleDto);

        return "redirect:/article/content/" + savedArticleDto.getId();
    }
```

**서비스 계층 코드**

```java
		// 게시글 저장
    @Transactional
    public ArticleDto create(ArticleDto articleDto) {

        User user = getCurrentUser();

        Article article = new Article(
                articleDto.getTitle(),
                articleDto.getContent()
        );

        article.setUser(user);

        Article savedArticle = articleRepository.save(article);
        return mapToArticleDto(savedArticle);
    }
```

- 게시글 작성 URL은 SecurityFilterChain 설정을 통해 인증된 유저만 접근 가능하도록 설정했기에 특별한 검증 작업 없이 진행한다.

#### **게시글 삭제**

```java
		// 게시글 삭제
    @Transactional
    public void deleteArticle(Long id) {

        User user = getCurrentUser();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (article.getUser().getId().equals(user.getId()) || user.getRole() == Role.ADMIN) {
            articleRepository.delete(article);
        } else {
            throw new NotHaveAuthorityException("게시글을 삭제할 권한이 없습니다.");
        }

    }
```

- 서비스 계층의 getCurrentUser() 메서드를 이용하여 현재 사용자 정보를 가져온다.
- article.getUser().getId().equals(user.getId()) || user.getRole() == Role.ADMIN 코드를 통해 일반 유저일 경우 해당 게시글의 유저 아이디와 조회한 유저의 아이디가 일치할 경우 삭제를 진행한다.
- 또는 현재 조회한 사용자의 권한이 관리자일 경우 게시글 삭제를 진행한다.

#### **게시글 수정**

```java
		// 게시글 수정
    @Transactional
    public ArticleDto update(ArticleDto articleDto) {

        User user = getCurrentUser();

        Article article = articleRepository.findById(articleDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (article.getUser().getId().equals(user.getId()) || user.getRole() == Role.ADMIN) {
            article.setTitle(articleDto.getTitle());
            article.setContent(articleDto.getContent());
        } else {
            throw new NotHaveAuthorityException("게시글을 수정할 권한이 없습니다.");
        }

        return mapToArticleDto(article);

    }
```

- 서비스 계층의 getCurrentUser() 메서드를 이용하여 현재 사용자 정보를 가져온다.
- article.getUser().getId().equals(user.getId()) || user.getRole() == Role.ADMIN 코드를 통해 일반 유저일 경우 해당 게시글의 유저 아이디와 조회한 유저의 아이디가 일치할 경우 게시글 수정을 진행한다.
- 또는 현재 조회한 사용자의 권한이 관리자일 경우 게시글 수정을 진행한다.
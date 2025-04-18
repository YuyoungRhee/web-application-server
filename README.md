# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* **Thread에서 start() vs run() 차이**
  - run(): 현재 스레드에서 메서드 실행 (단순 메서드 호출)
  - start(): JVM이 새 스레드 생성 -> run() 실행 (멀티 스레딩 발생)
  ```java
    Thread t = new Thread(() -> System.out.println(Thread.currentThread().getName()));

    // Case 1: 직접 run 호출
    t.run(); // "main" 이 출력됨 → 현재 스레드에서 실행
    
    // Case 2: start 호출
    t.start(); // "Thread-0" 이 출력됨 → 진짜 스레드에서 실행
  ```
- **InputStream**: 데이터를 읽는 스트림 (외부 -> 내 프로그램)
  - read()는 1바이트씩 읽음
  - 데이터 없으면 블로킹 (계속 기다림)
  - -1은 EOF (파일 끝 또는 연결 끊김)
- **OutputStream**: 데이터를 보내는 스트림 (내 프로그램 -> 외부)
  - “사용자에게 응답을 넘긴다” = OutputStream을 통해 HTTP 메시지를 보내는 것
  - write(byte[]): 바이트 배열을 클라이언트로 전송
  - flush(): 버퍼에 쌓인 데이터를 강제로 보냄 (네트워크 통신에선 매우 중요)

- **OutputStream은 반드시 flush() 해줘야 할까?**

    ✔ 네트워크 스트림은 내부에 버퍼가 있음

    → write()만 해도 당장 전송 안 됨

    → flush()를 호출해야 진짜 전송
- **BuffredReader** 
  
    → 네트워크나 파일에서 1바이트씩 읽는 건 엄청 비효율적

    → 그래서 나오는 게 BufferedInputStream, BufferedReader 같은 “버퍼링된 스트림”

    | 구분 | InputStream | BufferedReader |
    |------|-------------|----------------|
    | 역할 | **바이트(byte)** 단위로 읽음 | **문자(character)** 단위로 읽음 |
    | 타입 | 이진 데이터 (이미지, 바이너리 등) | 텍스트 (문자열 등) |
    | 속도 | 느림 (1바이트씩 읽으면 비효율) | 빠름 (버퍼 사용) |
    | 사용 시기 | 파일, 소켓 등에서 로우 데이터 읽을 때 | 텍스트 기반 프로토콜 (HTTP 등) 읽을 때 |

- **DataOutputStream**
  - DataOutputStream은 OutputStream을 감싸서 더 편하게 기초 데이터 타입이나 문자열을 쉽게 쓸 수 있게 해주는 보조 스트림.
    - FilterOutputStream을 상속한 클래스 
    - 내부적으로 OutputStream을 들고 있고, 거기에 기능을 추가한 구조
  - OuputSteam 문제점
    - 문자열마다 getBytes() 해야 되고
    - int, short, long 같은 숫자도 전송 불편 → 너무 저수준이라 귀찮고 가독성도 떨어짐

- **Content-Type**
  - Content-Type은 서버가 응답하는 데이터의 타입을 나타내는 HTTP 헤더. 브라우저는 이 헤더를 보고 “어떻게 해석할지” 결정함
  1. CSS가 적용되지 않아 확인해보니 서버가 Content-Type: text/html로 응답하고 있었음.
  2. 브라우저는 Content-Type을 기준으로 파일을 해석하므로, 잘못된 타입이면 CSS로 인식하지 않음.
  3. 순수 자바 웹서버였기 때문에 .css, .js 요청 시 Content-Type을 switch문으로 직접 설정함.
      -  Spring Boot는 요청 확장자나 컨트롤러 반환 타입에 따라 Content-Type을 자동 설정함.
  4. 이를 통해 브라우저가 리소스를 올바르게 해석하고 스타일이 정상 적용되었음.

### 요구사항 2 - get 방식으로 회원가입
* 201 Created
  * 새로운 리소스가 성공적으로 생성되었을 때 사용하는 상태코드
  * "생성됨"을 알리는 것뿐만 아니라, "어디에 생성되었는지"를 클라이언트에게 알려줌
    - RESTful API - Location 헤더에 새로운 리소스의 URI 경로를 담음 / body에는 새로운 리소스의 정보를 담음
    - RESTful하지 않은 API
      - 선택지 1: Location에 논리적으로 접근 가능한 URL을 줌
        ```http request
        HTTP/1.1 201 Created
        Location: /user/profile?userId=user
        ```
      - 선택지 2: Location 없이, 바디에만 생성 결과를 담는다
        ```http request
        HTTP/1.1 201 Created
        Content-Type: application/json
        
        {
        "userId": "user",
        "name": "멍구",
        "profileUrl": "/user/profile?userId=user"
        }
        ```

### 요구사항 3 - post 방식으로 회원가입
- form으로 POST 요청을 보내면, 파라미터 데이터는 HTTP body에 들어간다


- GET vs POST
  - Content-Type
    - GET: 바디가 없기 때문에 필요 없음
    - POST: 대부분 존재 (PUT, PATCH 같이 Request에 body가 있다면 대부분 명시필요)
    - 대부분의 서버 프레임워크(Spring)는 GET 바디를 무시함.
    
    **주요 Content-Type 종류**

    | Content-Type | 설명 | 주로 사용되는 곳 |
    |--------------|------|------------------|
    | `application/x-www-form-urlencoded` | `key=value&key2=value2` 형식 | ✅ HTML `<form>` 기본 전송 방식 |
    | `multipart/form-data` | 파일 업로드용 (바운더리로 구분) | ✅ 이미지, 파일 전송 |
    | `application/json` | JSON 포맷 데이터 | ✅ REST API, JS fetch/ajax |
    | `text/plain` | 그냥 문자열 | 디버깅용, 테스트 |
    | `application/xml` | XML 포맷 | 일부 레거시 시스템 |

### 요구사항 4 - redirect 방식으로 이동
* 302 Found
  * 302 Found는 요청한 자원이 일시적으로 다른 URI에 위치한다
  * 이 리다이렉트는 임시적인 것이므로, 클라이언트는 다음 요청부터도 원래의 URI를 계속 사용해야 합니다.
  * 서버는 Location 헤더를 반드시 포함해서 클라이언트가 리다이렉트할 수 있게 해야 합니다.
  *  브라우저(사용자 에이전트)가 자동으로 리다이렉트할 경우,
     원래 POST였으면 리다이렉트 요청도 POST로 유지해야 한다
     * 하지만 대부분의 브라우저는 무조건 GET으로 바꿈
      → 결과적으로 POST → GET으로 리다이렉트됨
      → 이건 명세 위반이었지만 현실에서는 널리 사용됨
     - 303 See Other: 아예 GET으로 바꾸라고 명시함(POST 이후 리다이렉트에 적합)
     - 307 Temporary Redirect: 명세대로 메서드를 그대로 유지(POST -> POST)
     
       | 상황 | 추천 상태 코드 |
       |------|----------------|
       | REST API POST 후 생성된 리소스 보여주기 | `201 Created + Location: /resource/{id}` |
       | 영구 리다이렉트 | `301 Moved Permanently` |
       | form POST 후 결과 페이지 이동 | `303 See Other` |
       | 임시 리다이렉트 (같은 메서드 유지) | `307 Temporary Redirect` |


### 요구사항 5 - cookie
**SameSite**

- SameSite=Lax: Same-Site 요청만 쿠키 보냄 (GET + 안전한 요청만)
- SameSite=None: Cross-Site에도 쿠키 보냄 → 반드시 Secure 필요 (HTTPS 필수)
- SameSite=Strict: 진짜 같은 사이트에서만 쿠키 보냄

1. ❗ 쿠키: SameSite 속성
   •	SameSite=Lax: Same-Site 요청만 쿠키 보냄 (GET + 안전한 요청만)
   •	SameSite=None: Cross-Site에도 쿠키 보냄 → 반드시 Secure 필요 (HTTPS 필수)
   •	SameSite=Strict: 진짜 같은 사이트에서만 쿠키 보냄

2. ❗ CSRF 공격 방지
   •	Cross-Site 요청 + 자동 전송되는 쿠키 → 공격 가능
   •	그래서 SameSite=Lax가 기본이 됨

3. ❗ CORS (Cross-Origin Resource Sharing)
   •	Cross-Origin 요청 시, 서버가 명시적으로 허용 안 하면 차단
   •	즉, Cross-Site 요청은 쿠키, Authorization 헤더 전송 등 다 막힘 (보안 위협 때문)

- 브라우저의 SameSite 기본 정책
  - Chrome, Edge, Firefox는 기본적으로: SameSite=Lax 정책을 자동으로 적용

브라우저가 Same-Site 와 Cross-Site를 분류하는 방법
![img.png](img.png)

**Path**
- URL 중에서 도메인 다음에 오는 리소스 경로를 의미해.
    https://example.com/user/profile/edit

  - 여기서 /user/profile/edit 이게 바로 HTTP에서 말하는 Path
    
    → 쿠키에는 Path 속성을 지정할 수 있음
    
    → “이 경로 이하에서만 이 쿠키를 전송하라!“는 의미
```http request
Set-Cookie: token=abc123; Path=/admin;
```
→ 이 쿠키는 /admin, /admin/settings, /admin/dashboard 등 하위 경로 요청에만 자동으로 포함됨

하지만 / 또는 /user, /login 같은 경로에서는 자동 전송 ❌ 안 됨
- 브라우저는 기본적으로: Set-Cookie를 보낸 응답의 URL 경로를 Path로 사용함

    ``` http request
  POST /user/login 
  Set-Cookie: logined=true
    ```
  이러면 Path가 자동으로 /user가 됨

    ✔ 그러면 /user/... 요청엔 쿠키가 붙지만 
    
    ❌ /index.html에는 자동 포함 안 됨

**WWW-Authenticate**

WWW-Authenticate는 서버가 클라이언트에게 “인증 방식”을 알려주는 HTTP 응답 헤더

"너 이 요청하려면 인증 필요해! 어떤 방식으로 인증해야 하는지 알려줄게!"

1. Basic 인증
   ```http request
   HTTP/1.1 401 Unauthorized
   WWW-Authenticate: Basic realm="Admin Area"
   ```
   - Basic → 인증 방식 (ID, PW를 base64 인코딩해서 보내는 방식)
   - realm="..." → 인증 대상 영역 (UI에서 보여줄 이름)
     - 같은 서버라도 realm이 다르면 다른 인증 정보로 간주돼

     👉 브라우저는 이걸 보고 로그인 팝업창을 자동으로 띄우기도 함!

2: Bearer (OAuth2 토큰 기반 인증)
```http request
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="example", error="invalid_token"
```
→ 이건 API 요청에서 자주 씀
→ 클라이언트에게 “Bearer 토큰을 써야 해”라고 말해주는 것

✅ 클라이언트는 이걸 보고 뭘 하냐?
1.	서버가 401 + WWW-Authenticate 헤더를 보내면
2.	클라이언트는 거기에 맞는 인증 정보를 Authorization 헤더에 담아서 다시 요청

❗ 주의할 점
- Authenticate는 401 Unauthorized 응답에서만 나와야 한다
- 없으면 → 브라우저는 인증 요구인지 모름
- 너무 막 쓰면 → 브라우저 로그인 팝업이 튀어나올 수 있음!

### 요구사항 6 - stylesheet 적용
**Content-Type**

Content-Type은 HTTP 메시지 바디의 데이터 형식을 명시하는 헤더야.

즉, “지금 보내는/받은 데이터는 이런 타입이야!” 라고 말해주는 거지.

ex)
- text/html
- application/json
- multipart/form-data
- image/jpeg, image/png
- application/x-www-form-urlencoded

Spring에서 Content-Type을 결정하는 주체는 → HttpMessageConverter라는 놈이야.
-  HttpMessageConverter가 요청/응답 내용을 보고
   - 어떤 타입인지 판단하고
   - Content-Type 헤더를 자동으로 설정해줌

그리고 그걸 DispatcherServlet → HandlerAdapter → 메시지 컨버터 단계에서 자동 처리함.

```
요청 → DispatcherServlet → HandlerMapping → Controller
                                  ↓
                             return 값
                                  ↓
                (ResponseBody 또는 @RestController면)
                        → HttpMessageConverter
                                  ↓
                 → Content-Type 자동 결정 + 직렬화
```

### heroku 서버에 배포 후
* 

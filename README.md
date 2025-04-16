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
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 

---
title: "[디버깅] NoHttpResponseException 사례"
date: 2022-02-04 18:00:00
---

http 1.0 이상 요청에서 `org.apache.http.NoHttpResponseException: {host} failed to respond` 에러에 대한 대응을 기록한다.


### 해결

HttpConnectionPool 의 `validateAfterInactivity` 옵션을 200ms 로 짧게 지정하였다.

### 에러 원인, 그렇게 처리한 이유

ResponseException 이라 서버에서 잘못된 응답을 준건가? 하고 낚일 수 있다. 

이번 에러는 클라이언트가 CLOSED_WAIT 상태에서 연결종료 응답을 받길 기대한 것으로, 
이미 종료된 커넥션 상에서 응답을 기대한 것이다.

`HttpRequestExceutor.java` 의 `execute` 함수를 살펴보겠다. 이 함수는 리퀘스트를 보내는 함수이다.

```java
public HttpResponse execute(...) throws IOException, HttpException {
    ...
    try {
        HttpResponse response = doSendRequest(request, conn, context);
        if (response == null) {
            response = doReceiveResponse(request, conn, context);
        }
        return response;
    } catch (final IOException ex) {
        ...
}
```

`doSendRequest` 구현체를 열어보면

```java
protected HttpResponse doSendRequest(...) throws IOException, HttpException {
    ...
    HttpResponse response = null;
    ...
    if (request instanceof HttpEntityEnclosingRequest) {
       // Check for expect-continue handshake. We have to flush the
        // headers and wait for an 100-continue response to handle it.
        // If we get a different response, we must not send the entity.
       ...
        if (((HttpEntityEnclosingRequest) request).expectContinue() &&
            !ver.lessEquals(HttpVersion.HTTP_1_0)) {
            ...
            // As suggested by RFC 2616 section 8.2.3, we don't wait for a
            // 100-continue response forever. On timeout, send the entity.
            if (conn.isResponseAvailable(this.waitForContinue)) {
                ...
            }
        }
       ...
    }
    ...
}
```

handsshake 란 주석이 학부생 때 배웠던 네트워크 시간을 떠올리게 한다.


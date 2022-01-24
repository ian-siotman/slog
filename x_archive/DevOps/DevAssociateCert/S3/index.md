## S3

### Buckets

- s3 는 오브젝트 (파일) 을 버킷 (디렉토리) 에 저장할 수 있게한다.
- 버킷 이름은 클로벌 유니크 네임
- 버킷은 리전 레벨로 정의
- 네이밍 컨벤션
    - 대문자 불가
    - 언더스코어 불가
    - 3-63 글자
    - 아이피 불가
    - 숫자나 소문자로 시작
    
### 오브젝트

- 오브젝느는 키를 갖는다
- 키는 풀패스임
    - s3://bucket/folder1/file.txt
- 키는 프리픽스 + 오브젝트 이름 (프리픽스는 버킷 ... 오브젝트 이름 사이)
- 버킷에는 디렉토리라는 개념이 없음
- 그냥 키는 슬래쉬가 포함된 긴 이름일 뿐임

- 오브젝트 바디의 내용
    - 5TB 까지
    - 5기가 이상이면 멀티파트 업로드 를 사용해야함
    
- 메타데이터 (키-발루 페어 리스트 - 시스템 혹은 유저 메타데이터)
- 태그 (유니크 키 / 발루 페어 - 10개까지) : 보안 / 라이프사이클에 유용
- 버전 아이디 (버져닝이 활성화되있다면)


### s3 versioning

- 버저닝 할 수 있음
- 이건 버킷 레벨
- 같은 키 오버라이트는 버전을 증가시킴
- 버킷을 버저닝하는 건 좋은 사례
    - 삭제 방지 (버전으로부터 복원 능력)
    - 롤백
    
- 노트
    - 버저닝 활성화 전 파일들은 버전 널임
    - 버저닝 서스펜딩(일시정지)은 이전 버전을 삭제하지 않음
    
### s3 오브젝트 암호화

- There are 4 methods of encrypting objects in S3
    - SSE-S3: encrypts S3 objects using keys handled & managed by AWS
    - SSE-KMS: leverage AWS Key Management Service to manage encryption keys - SSE-C: when you want to manage your own encryption keys
    - Client Side Encryption

- 어떤상황에 어떤걸 적용할지 이해하는게 중요

### SSE-S3

- s3 에 의해 관리되는 키로 인크립트
- 서버사이드에서 인크립트
- aes-256
- 헤더 필수 : "x-amz-server-side-encryption": "AES256"

### SSE-KMS

- KMS 로 관리되는 키로 인크립트
- 어드밴티지 : 유저 컨트롤, 어딧 트레일
- 서버 사이드
- 헤더 필수 : "x-amz-server-side-encryption": "aws:kms"

### SSE-C

- 내가 제공한 키로 인크립트
- 서버사이드
- s3 가 제공한 키를 저장하진 않음
- HTTPS 필수
- 모든 요청에 헤더 제공 필수

### Client Side Enc.

- Amazon S3 Encryption Client 와 같은 클라이언트 라이브러리 사용
- s3 보내기전에 암호화
- 클라이언트가 s3 에서 받아서 복호화
- 키와 암호화 사이클을 모두 직접 관리

### 트랜짓 상 암호화 (SSL/TLS)

- 노출된 것은:
    - HTTP endpoint: non enc.
    - HTTPS endpoint: 인플라이트 암호화
    
- https 가 추천됨
- 대부분 클라이언트가 https 를 디폴트로 사용
- https 는 sse-c 에 필수

### S3 보안

- 유저베이스
    - IAM 폴리시 - 어떤 api 콜을 IAM 콘솔 상 유저에 허용할 것인가.
- 리로스 베이스
    - 버킷 폴리시 - s3 콘솔 상 버킷 와이드 룰 - 크로스 어카운트 어세스
    - Object Access Control List (ACL) - finer 그레인
    - 버킷 ACL - less common
    
- IAM 프린시펄은 S3 오브젝트에 어세스할 수 있는 경우
    - IAM 퍼미션이 이걸 허용하거나 리소스 폴리시가 이걸 허용할 경우
    - AND 명시적 디나이가 없을 경우
    
### S3 버킷 포리시

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicRead",
      "Effect": "Allow",
      "Principal": "*",
      "Action": [
        "s3:GetObject"
      ],
      "Resource": [
        "arn:aws:s3:::examplebucket/*"
      ]
    } 
  ]
}
```

- 제이슨 베이스
    - Resources: buckets and objects
    - Actions: Set of API to Allow or Deny
    - Effect: Allow / Deny
    - Principal:The account or user to apply the policy to
    
- Use S3 bucket for policy to:
    - Grant public access to the bucket
    - Force objects to be encrypted at upload
    - Grant access to another account (Cross Account)
 
### Block Public Access 를 위한 버킷 세팅

- 오브젝트, 버킷 블록 퍼플릭 어세스는
    - new acl
    - any acl
    - new 퍼플릭 버킷 혹은 어세스 포인트 폴리시
    
- Block public and cross-account access to buckets and objects through any public bucket or access point policies
- These settings were created to prevent company data leaks
- If you know your bucket should never be public, leave these on
- Can be set at the account level

### Security Other

- 네트워킹
    - VPC 엔드포인트 지원
- 어딧 로깅
    - 에서스 로그
    - API 콜 로그 (CloudTrail)
        
- 유저 시큐리티:
    - MFA 삭제
    - Pre-Signed URLs : 제한된 시간동안 밸리드
    
### S3 Websites

- S3 can host static websites and have them accessible on the www
- The website URL will be:
    - <bucket-name>.s3-website-<AWS-region>.amazonaws.com OR
    - <bucket-name>.s3-website.<AWS-region>.amazonaws.com
- If you get a 403 (Forbidden) error, make sure the bucket policy allows public reads!

### CORS

- origin : 스킴 (프로토콜), 호스트(도메인) 그리고 포트
- CORS : Cross-Origin Resource Sharing
- 웹브라우저는 메인 오리진 방문했을 때 다른 오리진에 요청할 때 그 오리진이 허락된 리퀘스트만 가능하다는 메커니즘갖음
- Same origin: http://example.com/app1 & http://example.com/app2
- Different origins: http://www.example.com & http://other.example.com
- The requests won’t be fulfilled unless the other origin allows for the requests, using CORS Headers (ex: Access-Control-Allow-Origin)

메인오리진 접속 > 크로스요청 > 프리플라이트 (크로스 요청 가능하냐는 질문) > 뭐뭐가능한지 알려줌 > 실요청

### S3 CORS

- 클라이언트가 우리 s3 에 크로스 오리진 요청한다면, 올바른 CORS 헤더를 활성화해야함
- * 로 모든 오리진에 허가 가능
- 크로스 오리진 활성화된 버킷에 해당함

### S3 동시성 모델

- Strong consistency as of Dec 2020:
- After a:
    - successful write of a new object (new PUT)
    - or an overwrite or delete of an existing object (overwrite PUT or DELETE)
    
- ...any:
    - subsequent read request immediately receives the latest version of the object
    (read after write consistency)
    - subsequent list request immediately reflects changes (list consistency)
    
- Available at no additional cost, without any performance impact

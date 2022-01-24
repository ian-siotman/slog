# ELB & ASG

- Scalability : 더 큰 부하에 적응력있게 확장한단 말
    - Veritical Scalability
    - Horizontal Scalability ( = elasticity )
     
- Vertical Scalability
    - 인스턴스 사이즈를 키우는 것
    - 주니어 -> 시니어
    - t2.micro -> t2.large
    - RDS, ElastiCache 등
  
- Horizontal Scalability
    - 숫자를 늘리는 것 
    - 분산 시스템을 암시
    - 일반적이다
    - ec2 에서 쉽다
    
- High Availability
    - 보통 수평 스케일링이 따른다.
    - 어플리케이션을 두 az 에 나눠 돌린다는 걸 뜻한다.
    - 하나 죽어도 괜찮게 하는게 목적
    - 동적일 수 있음

ec2 에서는 scale up / down / out / in  으로 부름
 
 
## ELB

- 로드밸런스임 엘라스틱 로드밸런서. 
- 멀티 다운스트림 인스턴스에 분산하면서도 DNS 통해 싱글 어세스 포인트 노출
- 다운스트림 인스턴스 실패 핸들링 좀더 쉬워짐
- 헬스 체킹
- ssl 터미네이션 제공
- 쿠키 stickiness 강제
- az 간의 높은 가용성
- 퍼블릭/프라이빗 트래픽을 나눌 수 있음

- managed load balancer 임
    - aws 가 보장
    - 유지보수 업ㄷ그레이드 등등 다 해줌
    - 설정할게 얼마없음
- 직접 만드는것보다 쌈
- aws 서비스랑 인터그레이션 쉬움

### Health Checks

- ec2 인스턴스에서 200 리스폰스 주는지 체크해줌

### 로드밸런서 타입

- Cassic Load Balancer (v1 - old) - 2009
    - HTTP, HTTPS, TCP
    
- Application Laod Banlancer (v2 new gen) - 2016
    - HTTP, HTTPS, WebSocket
    
- Network Load Balancer (v2 new gen) - 2017
    - TCP, TLS (secure TCP) & UDP
    
- Gateway Load Balancer 도 나옴 - 2020
    - 이건 살짝 나중에 VPC 할 때 공부하겠음

- new gen 을 쓰는게 좋다 기능이 더 많다.
- 인터널(프라이빗) 혹은 익스터널(퍼블릭) ELB 를 셋업할 수 있음

### 시큐리티 그룹

- 유저는 어디서든 ELB 에 요청하면, ELB 는 요청에 대해 보안그룹에 따라 정책을 적용할 수 있음
- ELB <> EC2 에서 무조건 로드밸런서에서만 요청받게 스트릭트하게 보안그룹 적용할 수 있다.

### 알아두면 좋을것들

- LB 는 스케일 가능하지만 동시에 할순 없으니, 스케일이 매시브하면 웜업을 위해 aws 에 컨택트해야함
- 트러블슈팅
    - 503 가 로드밸런서 에러로 볼수있고, 케파가 부족하거나 타깃이 없다는 말
    - 어플리케이션에 커넥트가 안되면 보안그룹을 확인할만하다.
- 모니터링
    - ELB access logs 가 모든 어세스 리퀘스트를 로깅한다.
    - CloudWatch Metrics 가 매트릭스 어그리게이션 해준다. (ex: connections count)


    

## CLB

- TCP (L4), HTTP, HTTPS (L7)
- Health check 는 TCP, HTTP 베이스
- Fixed hostname
    - XXX.region.elb.amazonaws.com
    
### 실습 

우선 ec2 만들어두자

사이드바 메뉴 밑쪽에 Load Balancing > Load Balancers > Create

4개 옵션 볼 수 있음 CLB 는 회색인걸 볼수 있음. 일단 만들어보자

Create > 이름 짓고 VPC 는 디폴트

인터널로 설정할 수 있음을 볼 수 있음.
VPC 설정할 수 있음을 볼 수 있음
리스너 프로토콜 등을 정할 수 있음

다음 > 보안그룹설정 > http 80 all > 헬스체크

포트 패스 등을 정할 수 있음. ec2 의 헬스체크용 엔드포인트 적어주면됨 
get 가능한거 아무거나해도 무방. 200 만 체크하니
타임아웃, 인터벌 등등 설정 가능

- unhealthy threshold : 설정한 값 만큼 200 아닌 스레드가 있으면 안 건강한 것
- healthy threshold : 설정한 값 만큼 200 인 스레드가 있으면 건강한 것

다음 > ec2 추가 > 생성

로드밸런서 메뉴에서 만든 거 눌러보면 상태 볼 수 있음

인스턴스 탭 > Status
- OutOfService
- InService
상태 볼 수 있음. 실행되기까지 조금 걸림

clb dns 이름 으로 get 요청하면 ec2 에 잘 도달하는 걸 볼 수 있음.

ec2 의 보안그룹에서 퍼블릭 보안정책 없애고 테스트해보자 
그럼 로드밸런서도 접근 안된다.

그러니 ec2 보안그룹 편집할 때 80 all 하지말고 80에 clb 의 보안그룹 붙이면 된다.
그럼 ec2 퍼블릭 호스트로는 접근 안되고 clb 로만 접근 가능하다.

ec2 두개정도 더 생성해보자

로드밸런서 메뉴에서 clb 클릭하고 

인스턴스 탭 > 인스턴스 편집 > 만든 ec2 추가

그럼 clb 에 인스턴스 3개 붙음

clb 에 요청해보면... 3개에 로드밸런싱 되는걸 볼 수 있음

해보고 지우는거 잊지말자.

## ALB

- L7 - HTTP
- target groups 라 불리는 http 앱들에 로드 밸런싱 한다
- 같은 머신에 여러 로드 밸런싱도 가능 (ex: containers)
- http/2 & WebSocket 지원
- 리다이렉트 지원 (http -> https 같은)

- 다른 타겟 그룹들에 루트 라우팅 지원
    - url path base
    - url hostname base
    - query String, Headers base
- msa, container-base app 들에 좋음
- ecs 에서 다이나믹 포트에 리다이렉트할 수 있도록 포트 매핑 기능 있음
- 반면 clb 는 앱당 여러 개가 필요

### Target Groups

- ec2 instances (ASG 로 관리 가능) - http
- ECS tasks (ECS 자체로 관리됨) - http
- Lambda functions - Http Request 가 json 이벤트로 전환됨
- IP Addresses - 프라이빗 아이피들이어야함

- 여러 타겟그룹에 라우트할 수 있음
- 헬스 체크가 타겟그룹 레벨에 있음

### Good to Know

- fixed hostname 제공
- 어플리케이션 서버는 클라이언트의 ip 를 직접적으로 볼 수 없음
    - X-Forwarded-For 로 봐야함
    - X-Forwarded-Port, X-Forwarded Proto
    
## NLB
 
- L4
    - TCP / UDP 를 포워딩
    - 퍼포먼스 좋음
    - 레이턴시 적음 ~ 100 ms (vs 400 ms for ALB)
- static IP per AZ, Elastic IP 지원
    - specific IP 화이트 리스팅에 유용
- 퍼포먼스가 필요하거나 TCP or UDP 에 대응해야하면 씀
- 프리티어에는 지원되지 않으므로 주의

## Sticky Sessions

- 같은 인스턴스에 붙어있을 수 있도록 하는 것
- CLB, ALB 에서 킬 수 있음
- 만료시간 제어를 위해 쿠키를 씀
- 세션데이터 유지하고 싶을 때 쓸 수 있음
- 부하분산 밸런스가 깨질 수 있음

### 쿠키 이름

- Application-based Cookies
    - Custom cookie
        - 타겟이 만듬
        - 커스텀 어트리뷰트
        - 쿠키 이름이 각 타겟 그룹에 독립적으로 specified 되어있어야
        - 쿠키 이름 정할 때 AWSALB, AWSALBAPP, AWSALBTG 같은 예약어 있으니 주의 
    - Application cookie
        - LB 가 만들어 줌
        - AWSALBAPP 이란 이름으로 만들어 주
        
- Duration-based Cookies
    - LB 가 만들어줌
    - AWSALB, AWSELB (for CLB)
    
## Cross-Zone Load Balancing

- AZ 랑 상관없이 동일한 트래픽을 받게끔하는 옵션

- ALB
    - 항상 켜져 있음 (끌 수 없음)
    - inter AZ data 에 대해 비용청구 없음

- NLB
    - 꺼진게 디폴트
    - 켜져있담 inter AZ data 에 대해 비용청구됨
    
- CLB
    - 콘솔로 만들면 켜진게 디폴트
    - CLI / API 로 만들면 꺼진게 디폴트
    - inter AZ data 에 대해 비용청구 없음
    
## SSL/TLS 기본

- SSL 은 보안통신 (in-flight encrytion)
- SSL (Secure Sockets Layer) - 암호화 연결을 말함
- TLS (ㅅTransport Layer Security) - newer version
- 요즘은 TLS certs 가 주로 쓰이는데, 그냥 SSL 이라고 부른다.

- CA 로부터 공용 SSL certs 발급받는다.
    - 코모도, 시멘텍 등등

- SSL 서트는 일정주기마다 갱신해야됨

### LB 의 SSL

- X.509 certificate 씀
- ACM (AWS Certificate manager) 로 관리할 수 있음
- cert 업로드도 가능함
- HTTPS listener:
    - 기본 certifcate 를 명시해야함
    - 여러 도메인에 대한 옵셔날 서트 리스트를 추가할 수 있음
    - SNI (Server name Indication) 으로 호스네임을 명시하여 제공할 수 있음
    - older version ssl / tls 를 서포팅하기위한 보안정책을 명시할 수 있음
    
### SNI

- SNI 는 한 웹서버의 여러 SSL certificates 를 로드하는걸 할 수 있게 해줌
- newer 프로토콜로서, 클라이언트가 최초 ssl 핸드쉐이크에서 타깃 서버의 호스트 네임을 지칭해야함
- 그럼 서버가 옳은 서트를 주거나 디폴트를 줌
- ALB, NLB, CloudFront 에서만 동작함
- CLB 에선 안됨

- CLB
    - SSL 서트 1개
    - 여러개 쓸려면 여러 clb 써야됨

- ALB / NLB
    - 여러 SSL 서트와 리스너 가짐
    - SNI 씀

## Connection Draining

- 부르는 이름
    - CLB : Connection Draining
    - Traget Group : Deregistration Delay (for ALB, NLB)
    
- 인스턴스가 언헬시 하거나 de-registering 중 일때 in-flight requests 를 완료하는 시간
- 뺄 인스턴스에 대해 새로운 요청보내는걸 멈춤

- ec2 관점에서는 드레이닝 모드가 되면
    - 존재하는 커넥션 끝나길 기다림
    - 다른 인스턴스에 내로운 커넥션 성립됨

- 1 ~ 3600 초 사이에서 설정 가능하고 300 초가 디폴트
- 디스에이블 못함 (0으로 설정 안됨)
- 요청들이 짧으면 시간을 짧게 하면됨


## ASG 

- scale out / in 할수있게끔 해줌
- 자동으로 로드밸런서에 붙여줌
- 최소 / 최대 / desire 3개의 갯수를 설정함
- 실행 설정
    - AMI + Instance Type
    - EC2 User Data
    - EBS Volumes
    - 보안그룹
    - ssh 키페어
- 최소 / 최대 / 최초 케파
- 네트워크 + 서브셋 정보
- 로드밸런서 정보
- 스케일링 정책

### Auto Scaling Alarms

- CloudWatch 알람 베이스로 스케일할 수 있음
- 알람은 매트릭스를 모니터링
- 매트릭스는 asg 상 인스턴스의 오버올로 계산
- 알람베이스로 scale out / in 정책을 만들 수 있음

### Auto Scaling New Rules

- ec2 가 직접관리하는 자동 스케일링 룰을 정의할 수 있음
    - 타깃 평균 시피유 사용량
    - 인스턴스랑 elb 에 오는 요청 수
    - 네트워크 인아웃 평균

### asg 요약

- 스케일링 정책은 cpu, network 등등과 커스텀 메트릭스나 스케쥴로 정할 수 있다.
- 런치 템플릿을 쓸 수 있다.
- ASG 업데이트하려면 새로운 런치 설정과 템플릿을 제공해야한다.
- ASG 에 붙은 IAM 롤은 ec2 인스턴스에 어사인된다.
- ASG 는 공짜다. 언더라잉 리소스가 런치되는거에 부과된다.
- 인스턴스를 ASG 아래 둔다는 말은 그게 종료되면 ASG 가 자동으로 대체재로서 새로운걸 만든다는 말이다.
- LB 에 의해 unhealthy 로 마킹된 녀석들을 ASG 가 종료시킬 수 있다.

## ASG 스케일링 폴리시

### Dynamic Scaling Policies

- Target Tracking Scaling
    - 가장 쉬움
    - ex. ASG CPU overall usages 가 40% 일때
    
- Simple / Step Scaling
    - ex. CloudWatch 가 트리거되면 2개 추가하라
    - ex. CloudWatch 가 트리거되면 1개 제거하라
    
- Scheduled Actions

- Predictive Scaling
    - 예측하여 스케일링
    
스케일 판단에 좋은 메트릭스
    - CPU
    - RequestCountPerTarget
        - 타겟 당 (타겟그룹 X) 요청
    - 네트워크 인아웃
    - 커스텀 메트릭
    
### 스케일 쿨다운

- 스케일링 다음엔 쿨다운이 필요 (300초 디폴트)
- 쿨다운 동안엔 ASG 가 스케일링 안함
- 설정시간, 쿨다운 시간 등을 줄이기위해 바로 쓸수있는 AMI 를 쓰는게 좋음


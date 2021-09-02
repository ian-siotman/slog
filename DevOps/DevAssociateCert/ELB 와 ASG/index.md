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

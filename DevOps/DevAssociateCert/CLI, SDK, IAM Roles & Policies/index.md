## CLI, SDK, IAM Roles & Policies

### IAM Roles & Policies 추가

- 롤에서 폴리시를 볼 수 있는데, 폴리시를 직접 생성, 편집할 수 있음
- 임포트 할 수도 있음
- 인라인 폴리시도 가능
    - 그러나 추천되는 방식은 아님
- 매니지드 폴리시 쓰는게 낫김함

- 롤 메뉴에서 폴리시 열어보면 구조를 볼 수 있음

### 어떻게 제이슨 정의를 만드나

- create policy 에서 만들 수 있음
- 비주얼 에디터, 제이슨 에디터 지원
- AWS policy generator 도 있음

### Policy Simulator

- 폴리시 테스트할 수 있음 (구글링하면 바로 나옴)

### CLI dry Runs

- 폴리시 테스트만 하고 싶을 수 있음
- CLI 중 --dry-run 옵션 갖는 커맨드가 있고, 그걸 사용할 수 있음

### CLI STS Decode Errors

- api 콜 실패하면 긴 메세지 나옴
- 이 에러는 STS 커맨드 라인으로 디코딩 가능함
- sts decode-authorization-message (이것도 권한필요함)

### EC2 Instance Metadata

- 꽤 좋은데 많이 모름
- ec2 인스턴스가 그 목적에 해당하는 IAM Role 필요없이 그들 스스로 알 수 있도록 함
- http://169.254.169.254/latest/meta-data
- 메타데이터에서 IAM Role 이름정도 알 수 있고 폴리시 정보는 알 수 없음

### CLI profile

- 보통 ~/.aws 에 있는 config, credentials 만지면 됨
- aws configure --profile 프로파일이름 으로 설정도 가능
- aws 명령어에 --profile 붙이면 해당 프로파일로 실행가능함
- 혹은 export AWS_PROFILE=siotman

### MFA with CLI

- To use MFA with the CLI, you must create a temporary session
- To do so, you must run the STS GetSessionToken API call
- aws sts get-session-token --serial-number arn-of-the-mfa-device --token-code code-from-token --duration-seconds 3600
    - 여기서 얻는 크레덴셜로 접근해야함.
    - credentials 에 여기서 얻은 토큰(aws_session_token) 넣음
    
## SDK

- 디플트 리전 명시해야함

## AWS Limits (Quotas)

- API Rate Limits
    - DescribeInstances API for EC2 has a limit of 100 calls per seconds
    - GetObject on S3 has a limit of 5500 GET per second per prefix - For Intermittent Errors: implement Exponential Backoff
    - For Consistent Errors: request an API throttling limit increase

- Service Quotas (Service Limits)
    - Running On-Demand Standard Instances: 1152 vCPU
    - You can request a service limit increase by opening a ticket
    - You can request a service quota increase by using the Service Quotas API

### Exponential Backoff (any AWS service)

- If you get ThrottlingException intermittently, use exponential backoff
- Retry mechanism already included in AWS SDK API calls
    - Must implement yourself if using the AWS API as-is or in specific cases - Must only implement the retries on 5xx server errors and throttling
    - Do not implement on the 4xx client errors
    
### AWS CLI Credentials Provider Chain 

The CLI will look for credentials in this order

1. Command line options – --region, --output, and --profile
2. Environment variables – AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, and AWS_SESSION_TOKEN
3. CLI credentials file –aws configure
    - ~/.aws/credentials on Linux / Mac & C:\Users\user\.aws\credentials on Windows
4. CLI configuration file – aws configure
    - ~/.aws/config on Linux / macOS & C:\Users\USERNAME\.aws\config on Windows
5. Container credentials – for ECS tasks
6. Instance profile credentials – for EC2 Instance Profiles

### AWS SDK Default Credentials Provider Chain 

The Java SDK (example) will look for credentials in this order

1. Java system properties – aws.accessKeyId and aws.secretKey
2. Environment variables – AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
3. The default credential profiles file – ex at: ~/.aws/credentials, shared by many SDK
4. Amazon ECS container credentials – for ECS containers
5. Instance profile credentials – used on EC2 instances

### AWS Credentials Best Practices

- Overall, NEVER EVER STORE AWS CREDENTIALS IN YOUR CODE
- Best practice is for credentials to be inherited from the credentials chain

- If using working within AWS, use IAM Roles
    => EC2 Instances Roles for EC2 Instances
    => ECS Roles for ECS tasks
    => Lambda Roles for Lambda functions
- If working outside of AWS, use environment variables / named profiles


### Signing AWS API requests
- When you call the AWS HTTP API, you sign the request so that AWS can identify you, using your AWS credentials (access key & secret key)
- Note: some requests to Amazon S3 don’t need to be signed
- If you use the SDK or CLI, the HTTP requests are signed for you
- You should sign an AWS HTTP request using Signature v4 (SigV4)

- HTTP Header option 이 있음
- Query String option 이 있음 (s3 에 있는 이미지 바로 열어보면 나오는 쿼리 스트링 이 그 예)

# IAM

글로벌 서비스인 IAM(Idnetity and Access Management) 을 알아보자.

## 소개

### 기본 용어

- Root Account : 계정 생성 시 기본적으로 생성. 계정 설정 등 기본적인 셋업 외 공유 및 사용하지 않는 것을 권장.
- User : 사용자 단위로, Grouping 할 수 있다.
- Groups : user 를 포함하고, 그룹을 포함하지 않는다.

User 는 그룹 없이 단독으로 존재할 수 있고, 여러 그룹에 포함될 수도 있다.

### Permissions

Users, 혹은 Groups 에는 Policies 라 불리는 JSON 다큐먼트를 할당할 수 있다.
- **Policy** 는 각 User 의 **Permission** 을 정의한다.
- AWS 는 Least privilege principle 원칙을 적용하고 있다. (즉 필요최소 권한을 부여하도록 유도한다.)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ec2:Describe*",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": "elasticloadbalancing:Describe*", 
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "cloudwatch:ListMetrics",
        "cloudwatch:GetMetricStatistics",
        "cloudwatch:Describe*"
      ],
    "Resource": "*"
    }
  ]
}
```

## IAM Practice: 어드민 IAM 을 생성해보자.

1. 계정 생성 후, 루트 계정으로 IAM 콘솔에 접속
1. Access management > Users > Add User 
1. AWS Management Console access 타입을 생성
1. Next:Permissions
1. Create Group, 어드민 그룹으로 적절한 이름으로 생성
1. Policy 중 AdministratorAccess 를 추가
1. Next:Tags (스킵)
1. Next:Review
1. Create User
1. Credential CSV 받아둔다.
1. Users 목록에서 유서를 확인하고, 그룹도 확인해두자.
1. 그룹 메뉴에서 Permissions 도 확인해두자 
    - 그룹에서 상속됨을 확인할 수 있다.
1. IAM Dashboard 에서 Sign-in URL 을 확인할 수 있다.
1. 이름을 적절히 바꾸고, 다른 브라우저로 링크를 타면 IAM Sign-in UI 를 만날 수 있다.
1. 이 URL 이 아니더라도, IAM User 로그인을 선택 후, 
    Account NO 나 위에서 수정한 Alias 를 입력한 뒤, IAM 으로 로그인 할 수 있다.

## IAM Policies inheritance

IAM 폴리시는 상속받을 수 있다. 폴리시는 그룹과 개인에 부여될 수 있으므로, 
한 사용자가 그에게 부여된 폴리시 및 소속된 그룹의 폴리시를 적용받는다.


## IAM Policies Structure

### 구성요소

- Version : 폴리시 랭귀지 버전. 현재 "2012-10-17" 로 고정되어있다.
- Id : 폴리시의 아이디 (optional)
- Statement : 1 개 이상의 독립적인 선언들

### Statement 구성요소

- Sid : Statement 의 아이디 (optional)
- Effect : "Allow" or "Deny"
- Principal : 어떤 accunt / user / role 이 적용대상인가를 명시하는 곳
    - `{ "AWS": ["arn:aws:iam::123456789012:root"] }`
- Action : 이 폴리시가 Allow 혹은 Deny 하는 api 액션들의 리스트
    - `["s3:GetObject", "s3:PutObject"]`
- Resource : 액션들이 적용될 리소스 리스트
    - `[arn:aws:s3::bucketname/*]`
- Condition : 폴리시가 적용될 조건 (optional)

## IAM Password Policy

### Password Policy

아래와 같은 설정이 가능하다.

   - 최소 길이
   - 필수 포함 글자 (대소문자, 숫자, 특수문자 등)
   - 비밀번호 수정 가능 여부
   - 비밀번호 변경 강제 (비밀번호 만료 - 예를들어 90 일 이후 변경해야함)
   - 비밀번호 재사용 금지 (변경 시 같은 비번 사용 금지)
   
   
### MFA (Multi Factor Authentication)

MFA = password you know + security device you own

1. Virtual MFA device
    - Google Authenticator (Phone)
    - Authy (multi-device)
    
2. U2F(Universal 2nd Factor) Security Key
    - **YubiKey** by Yubico (3rd party)
    
3. Hardware Key Fob MFA Device
    - 오티피 같은 거
    

### Practice

IAM > Account Settings > Change Password Policy

여기서 Password Policy 를 설정할 수 있다.

---

오른쪽 위의 어카운트 네임 > My Security Credentials > MFA 패널 > Activate MFA

여기서 3개의 옵션 중 하나를 선택해서 활성화 시킬 수 있다.

## CLI, SDK 로 어세스하기 위한 Access Keys

Acess Id = username
Access Key = password

### CLI 란?

- shell 의 인터페이스
- public api 에 직접 접근
- 스크립트 개발 가능
- 오픈소스
- 웹 콘솔 안열어도 된다.

### SDK

- 개발 킷
- 많은 언어 지원한다.
- 임베딩 용

### Practice: Set-up (MacOS)

https://docs.aws.amazon.com/ko_kr/cli/latest/userguide/install-cliv2-mac.html

```
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /
```

### Practice: Access key 생성

IAM > Access management > Users > Security credentials 탭 > Access keys 섹션 > Create Access Key 버튼

```
aws configure
키 아이디 입력
키 입력
region 명 입력
default output format 은 그냥 엔터
```

아래 명령으로 확인가능 (현재 퍼미션으로 확인 가능할 때 리스트가 나온다.)

```
aws iam list-users
```

### AWS CloudShell

- 서울 리전은 지원 안한다. 
- 그냥 AWS 웹에서 제공하는 aws 가 깔려있는 터미널 정도로 생각하면 된다. 
- 업다운로드 가능하다.

## IAM Roles for AWS Services

permissions 와 비슷하지만, permissions 은 물리적인 사람이 쓰는 반면 이건 AWS Services 가 쓴다.

Common roles 에는 다음과 같은 것을 예를 들 수 있다.

- EC2 Instance Roles
- Lambda Function Roles
- Roles for CloudFormation 등

### Practice

IAM > Roles > Create role > AWS service 선택 > 아래에서 유즈 케이스를 선택 > EC2 로 만들어보자 > Next

폴리시 선택 > 예를 들어 IAMReadOnlyAccess > Next > Next

리뷰에서 이름을 적당히 지어주자

## IAM Security Tools

- IAM Credentials Report (account-level)
    - 여러 credentials 의 상태와 사용자들의 리스트를 리포트
    
- IAM Access Advisor (user-level)
    - 유저한테 부여된 서비스 퍼미션을 보여주고 서비스들의 마지막 어세스가 언제인지 알려준다.
    - 안쓰이는 폴리시를 알 수 있으니 유용
    
### Practice

IAM > Credential report > Download report

CSV 에서 여러 정보를 볼 수 있다.

IAM > Users > Access Advisor 탭

여기서 어떤 퍼미션으로 어세스 로그를 볼 수 있다.
    
## Best Practice

- root account 를 쓰지말자
- 1 user = 1 aws ser
- 그룹을 부여하고 퍼미션을 그룹에 붙이자
- 패스워드 폴리시를 잘 셋팅하자
- MFA 를 쓰도록 강제하자
- AWS Services 들에 Roles 을 부여하자
- 어세스 키를 이용해 CLI / SDK 를 사용하자
- IAM Credentials Report 를 잘 이용하자

## Summary

- Users : 비밀번호를 갖는 한 유저들의 집합
- Groups : 유저를 포함하는 것
- Policies : 그룹이나 유저의 퍼미션에 대한 아웃라인 JSON 도큐먼트
- Roles : AWS services 를 위한 것
- Security : MFA + Password Policy
- Access Keys : CLI 나 SDK 를 위한 어세스 방법
- Audit : IAM Credential Reports 와 IAM Access Advisor 사용 가능



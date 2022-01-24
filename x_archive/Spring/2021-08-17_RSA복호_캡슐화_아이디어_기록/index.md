RSA 유틸리티를 제작 중 유용한 아이디어를 기록차원에서 남긴다.

#### 원하는 것

1. 복호화 시 비즈니스 로직 상에서 똑같은 복호화 메소드를 매번 호출하기 싫다.
2. 복호화 사용법을 최대한 쉽고 간단하게 제공하고 싶다.


키 생성 규칙, 롤링, 만료규칙 등은 때에 따라 달라질테니 생략한다.
2030 년 이후 키 길이 2048 바이트가 안전 측면에서 보장되지 않는 것도 기억해두자.

#### 설계 1. validator 활용

1. `퍼블릭키`와 `만료토큰`을 전달받은 클라이언트가 암호화를 수행하고 서버로 전달
1. `RsaEncryptedField::class` 란 DTO 로 받는다. 예를 들어
    ```
        {
          "field": {
            "token": "{{token}}",
            "value": "{{encValue}}"
          }
        }
    ```
   (설마 당연히 기억하겠지만, 자료형에 따라 바이트가 다르니 해당 클래스 상속받아 타입별로 관리하거나 제너릭화한다.)
1. 이때, `RsaEncryptedField::class` 에는 `@field:JsonIgnore lateinit var decryptedValue: String?` 만들어둔다.
1. `@RsaEncrypted(적당한_인자)` 어노테이션을 만든다.
1. `적당한_Validator::class` 를 구현하여 밸리데이션 및 `decryptedValue` 에 값을 넣어준다.
1. 익셉션 대응

이렇게 하면 밸리데이션 + 디크립트를 동시에 해줄 수 있고, 애초에 밸리데이터라 익셉션에 따른 에러 리스폰스 주기도 쉽다.
또 명시적으로 `@RsaEncrypted` 호출해서 커스텀한 인자를 넣어 쓸 수 있으니 나름 이득이 있다. 
그러나 `@Valid`, InitBinder, 수동 밸리데이션 호출 등등을 통한 
validate 를 수행하지 않으면 decryptedValue 를 호출할 때 NPE 가 떨어질 것이다.
더하여, 이 DTO 에 decryptedValue 가 있는지 없는지 어쨌든 까봐야된다.

#### 설계 2. web 레지스트리 converter 활용

1. 위의 1~2 까지 동일
1. `RsaDecryptedField:class` 를 만든다.
1. 웹 포맷터 레지스트리에 `RsaDecryptedField:class` 의 `적당한_Converter::class` 를 구현 & 등록한다.
1. 익셉션 대응

RSA 복호화를 위해 호출하거나 알아야하는게 많이 줄어든다. 하지만 적당한 인자를 주어 가능성을 열어주기가 어렵다. 
예를 들어 `설계 1` 에서 제공한 한번 복호화하면 해당 퍼블릭키를 만료할지 말지를 결정하는 `expire` 플래그를 제공하려면
비슷한 클래스 `쓰면만료되는_RsaDecryptedField::class`, `만료안되는_RsaDecryptedField::class` 가 만들어진다.
`설계 1` 에서는 대충 인자 뚫어놓고 분기를 짜면 되는데 요건마다 클래스 하나하나 늘려가는게 귀찮을 수 있다.

#### 우선 설계 2 로 구현했다.

어쨌든 설계 2 로 구현했다. 코드리뷰가 들어오면 첨설한다.

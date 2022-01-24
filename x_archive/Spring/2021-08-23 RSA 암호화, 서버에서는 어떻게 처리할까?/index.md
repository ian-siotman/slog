# RSA 암호화, 서버에서는 어떻게 처리할까? 

렌딧에서는 사용자의 소중한 정보를 보호하기 위해 일부 정보에 대한 RSA 암호화를 적용하고 있습니다.
서버에서는 암호화된 정보를 받아 적절히 복호화하고, 비즈니스 로직에 전달해야할텐데요.
모든 내용을 공개할 순 없겠지만, 오늘은 구현한 RSA 암호화 로직과 유틸리티를 일부 소개하려합니다. 


## 왜 RSA 를 썼나요? 

암호화 방식 중 하나인 RSA 는 대표적인 비대칭키 알고리즘입니다. 


...

사용자의 정보가 들어있는 패킷을 누군가 중간에서 탈취하여 읽을 수 있다고한다면, 예기치 못한 사고가 의당 일어날 수 있습니다. 
443 포트 상 통신이라 하더라도 `SSL Sniffing` 과 같은 방법이 존재하는 만큼, 전송할 때에 미리 암호화하여 사고를 방지하는 것이 옳겠지요.
이러한 사고를 막기 위해 적절한 것이 RSA 암호화 방식입니다. 이 방식을 쓰면 누구나 암호화는 할 수 있지만, 누구나 복호화할 순 없거든요.

> SSL Sniffing
>
> 해커가 사용자에게 대상 HTTPS URL 을 HTTP 로 바꾸어 제공한 뒤, 요청문에서 평문을 읽어내고
> 이후 읽어낸 정보를 바탕으로 목적 서버와 SSL 통신을 수행하는 기법.
>
> 여기서 RSA 는 해커가 정보를 눈으로 읽을 수 없도록 방지하는 역할을 합니다.

정리하자면 공개키를 가진 누구나 암호화를 할 수 있지만, 개인키를 가진 곳에서만 복호화할 수 있다는 비대칭키 암호화 알고리즘의 이점을 바탕으로,
사용자 정보를 클라이언트에서 암호화하고, 서버에서만 복호화하여 나쁜사람들로부터 사용자 정보를 지키고자 RSA 를 사용했다고 할 수 있겠습니다. 

## 어떻게 RSA 를 썼나요?

### 공개키의 전달

렌딧에서는 RSA 암호화를 위해 여러 (공개키, 비밀키) 키페어를 미리 만들어둡니다. 오래된 키페어는 삭제하고요. 
클라이언트에서 공개키를 요청하면 살아있는, 즉 삭제하지 않은 공개키 중 랜덤하게 하나의 공개키를 보내드려요. 암호화하실 수 있도록 말이죠.

```kotlin
@GetMapping(URL)
fun getRandomKey(): Response<RsaKeyDto.PublicKey> {
    val publicKey = rsaCipherService.generate()
    return RestResponse.ok(publicKey)
}

fun RsaCipherService.generate(): RsaKeyDto.PublicKey {
    // 미리 만들어둔 활성 키 중 하나를 랜덤하게 가져와요.
    val key = getRandomRsaKey()
    
    // 복호화할 때 어떤 공개키를 썼는지 등에 대한 정보를 포함한 토큰을 만들어요. 
    // (여기에는 밸리데이션을 위한 몇가지 정보가 더 들어가지만, 주제와 멀어지니 생략할게요.)
    val token = shortLivedTokenService.generate(key.id, ...)

    // 토큰과 공개키를 반환해요.
    return RsaKeyDto.PublicKey(token.value, key.base64Pub)
}
```

### 복호화와 밸리데이션

위의 `URL` 로 얻은 공개키를 이용해 클라이언트에서 암호화하여 보내줍니다. 암호문을 받는 DTO 예제를 살펴볼까요?

```kotlin
data class WithdrawRequest(
    @field:RsaEncrypted(message = "유효하지 않은 출금금액입니다.")
    val reqDepositAmt: RsaEncryptedLong,
    ...
)

interface RsaEncryptedField {
    ...
    val value: String
}

data class RsaEncryptedStr(..., override val value: String, @field:JsonIgnore var decryptedValue: String?) : RsaEncryptedField
data class RsaEncryptedLong(..., override val value: String, @field:JsonIgnore var decryptedValue: Long?) : RsaEncryptedField
```

WIP

그 다음, Validator 레이어에서 복호화를 수행하도록 작업했어요.

```kotlin
class RsaEncryptedValidator : ConstraintValidator<RsaEncrypted, RsaEncryptedField> {
    ...

    override fun isValid(field: RsaEncryptedField, context: ConstraintValidatorContext): Boolean {
        try {
            rsaCipherService.decryptRsa(field, ...)
        } catch (e: Exception) {
            describeExceptionOrReThrow(e, context)
            return false
        }

        return true
    }

    private fun describeExceptionOrReThrow(e: Exception, context: ConstraintValidatorContext) = with(context) {
        val isExpectedException = when (e) {
            is DecryptException,
            is ShortLivedTokenException,
            is GeneralSecurityException -> true
            else -> false
        }

        if (!isExpectedException) throw e

        disableDefaultConstraintViolation()
        buildConstraintViolationWithTemplate(e.message)
    }
}

fun RsaCipherService.decryptRsa(encrypted: RsaEncryptedField, ...): RsaEncryptedField {
    ...

    when (encrypted) {
        is RsaEncryptedStr -> encrypted.decryptedValue = decryptRsaAsStr(encrypted.value, encKeyId)
        is RsaEncryptedInt -> encrypted.decryptedValue = decryptRsaAsInt(encrypted.value, encKeyId)
        is RsaEncryptedLong -> encrypted.decryptedValue = decryptRsaAsLong(encrypted.value, encKeyId)
        else -> throw NotImplementedError()
    }

    return encrypted
}
```

세가지 자료형



### 

## 마무리
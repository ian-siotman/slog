---
title: "Chained-Tx & QDSL - 2"
date: 2022-01-30 23:19:00
---

### Chained-Tx 는 Deprecated 였다.

spring-boot 버전은 2.2.3.RELEASED ( springframework 5.2.3 ) 이다. 

두가지 데이터소스에 대해, 트랜젝션을 어떻게 관리하는 지 궁금했다... 로 시작한 글이지만 방향이 바뀌었다.

ChainedTransactionManager 는 2.5 이후부터 Deprecated 되었다. 어떻게 대체할 지를 중심으로 글을 쓸 것이다.

사유는 [spring-data-commons 이슈](https://github.com/spring-projects/spring-data-commons/issues/2232) 링크를 참조할 것.

쓸 때는 몰라서 썼지만, 다행히도 한쪽에만 write 하는 로직들 뿐이었기 때문에 당장은 무방했다.

```kotlin
val newDb01Entities = buildDb01EntitiesFromDb02(refAt)
val oldDb01EntityMap = findOldDb01EntityMap(db01Entities)

// db01 에 대해서만 write 한다.
oldDb01EntityMap...onEach(db01Qdsl.entityManager::remove)
newDb01Entities...onEach(db01Qdsl.entityManager::persist)
```

그러나 Deprecated 인 이상, 대응해두어야한다. 열심히 만들어놓고 갈아엎어야하니 뼈아프다... 이런 실수는 없어야 할 것이다.

### 대안들

WIP
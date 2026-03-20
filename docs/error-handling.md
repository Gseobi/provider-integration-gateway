# Error Handling Notes

## 1. Overview

`provider-integration-gateway`는  
다수의 결제대행사(PG)를 연동하는 환경에서  
Provider 선택, 요청 데이터 구성, 공통 응답 반환 책임을 Backend 게이트웨이로 분리하는 구조를 기준으로 작성된 프로젝트입니다.

이 문서는 Provider 연동 과정에서 발생할 수 있는 주요 오류 상황과  
그에 대한 처리 방향을 정리한 문서입니다.

본 프로젝트는 Mock 기반 구조를 사용하므로  
실제 외부 연동 장애를 재현하는 것이 목적은 아니며,  
운영형 게이트웨이 구조에서 고려해야 할 오류 처리 기준을 정리하는 데 초점을 두고 있습니다.

---

## 2. Error Handling Direction

이 프로젝트에서 오류 처리는 다음 원칙을 기준으로 합니다.

- 잘못된 요청은 가능한 한 Provider 호출 전에 차단
- 지원하지 않는 Provider 요청은 명확히 실패 처리
- Provider 내부 차이는 Backend 내부에서 흡수
- 클라이언트에는 가능한 한 일관된 실패 구조 반환
- retry 가능한 오류와 즉시 실패해야 하는 오류를 구분

즉, 오류 처리는 단순히 예외를 던지는 것이 아니라,  
**어디까지를 Backend에서 책임지고, 어디부터를 Provider 특성으로 분리할지**를 정하는 문제로 보았습니다.

---

## 3. Unsupported Provider

### Problem
지원하지 않는 PG 코드로 요청이 들어오면  
어떤 Provider Strategy도 선택할 수 없습니다.

### Handling
- 요청된 PG 코드가 지원 대상인지 먼저 확인
- 매핑 가능한 Provider가 없으면 즉시 실패 처리
- 정상 결제 흐름으로 진행하지 않음

### Reason
미지원 Provider를 애매하게 처리하면  
잘못된 연동 요청이 정상 요청처럼 보일 수 있습니다.  
따라서 명확한 실패 응답을 반환하는 것이 더 안전합니다.

---

## 4. Request Validation Failure

### Problem
결제 요청에 필요한 필수값이 누락되거나 형식이 잘못된 경우,  
Provider 로직까지 진입하면 이후 단계에서 오류 원인 추적이 어려워질 수 있습니다.

### Handling
- Controller 또는 DTO 레벨에서 요청값 검증 수행
- Validation 실패 시 Provider 호출 전에 요청 차단
- 검증 실패 응답을 공통 형태로 반환

### Reason
요청 자체가 잘못된 경우는 외부 연동 실패가 아니라  
입력 데이터 문제이므로 가장 앞단에서 걸러내는 것이 적절합니다.

---

## 5. Invalid Provider Response

### Problem
실제 운영 환경에서는 Provider 응답이 누락되거나,  
예상과 다른 필드 형식으로 내려오거나,  
필수값이 비어 있는 경우가 발생할 수 있습니다.

### Handling Direction
본 프로젝트는 Mock 기반 구조이므로 실제 외부 응답 이상을 재현하지는 않지만,  
운영 구조 기준으로는 다음과 같은 방향을 고려했습니다.

- 필수 응답값 존재 여부 확인
- 예상 DTO 또는 내부 표준 구조로 매핑 가능한지 확인
- 매핑 불가능한 경우 Provider 응답 오류로 분류
- 클라이언트에는 공통 실패 응답 반환

### Reason
Provider마다 응답 형식은 다를 수 있지만,  
클라이언트가 그 차이를 직접 처리하게 하면  
연동 복잡도가 클라이언트로 전파됩니다.  
따라서 응답 이상도 Backend 내부에서 먼저 흡수하는 방향이 적절합니다.

---

## 6. Timeout and Retry Consideration

### Problem
외부 Provider 연동에서는 응답 지연이나 timeout이 발생할 수 있습니다.

### Handling Direction
본 프로젝트는 Mock 기반 구조로 timeout 자체를 구현하지는 않았지만,  
운영 환경 기준으로는 오류를 두 가지로 나눠 보는 것이 필요합니다.

#### Retry may be considered
- 일시적 네트워크 지연
- Provider 응답 지연
- 재시도해도 부작용이 적은 조회성 요청

#### Immediate failure is safer
- 결제 승인처럼 중복 요청 위험이 있는 작업
- 요청 멱등성이 보장되지 않는 작업
- 상태 변경이 이미 일부 반영되었을 가능성이 있는 작업

### Reason
모든 외부 호출을 단순 재시도하면  
결제와 같은 민감한 도메인에서는 중복 처리 위험이 생길 수 있습니다.  
따라서 timeout은 단순 예외가 아니라  
**재시도 가능 여부를 함께 판단해야 하는 오류**로 보는 것이 맞습니다.

---

## 7. Standardized Error Response

### Direction
Provider 내부 실패 원인은 다양할 수 있지만,  
클라이언트에는 가능한 한 일관된 실패 구조를 제공하는 방향을 기준으로 했습니다.

예를 들어 다음과 같은 형태를 생각할 수 있습니다.

- `code`
- `message`
- `provider`
- `retryable`
- `traceId` or request identifier

### Reason
공통 에러 응답 구조가 있으면 다음 장점이 있습니다.

- 클라이언트 처리 방식 단순화
- 운영 로그와 사용자 응답의 연결 용이
- Provider별 차이를 Backend 내부에 한정 가능
- retry 가능 여부 전달 용이

즉, 표준화의 목적은  
모든 오류를 똑같게 만드는 것이 아니라,  
**클라이언트와 운영 관점에서 해석 가능한 공통 형태를 유지하는 것**입니다.

---

## 8. Why Error Handling Was Not Fully Implemented

이 프로젝트는 실제 PG 승인 연동을 구현하는 것이 아니라,  
Provider 분기, 요청 데이터 구성, 책임 분리 구조를 설명하기 위한 포트폴리오입니다.

따라서 다음 항목은 실제 운영 수준으로 구현하지 않았습니다.

- 실제 외부 timeout 처리
- 서명 검증 실패 세부 분기
- Provider별 상세 에러코드 매핑
- 운영사별 계약 기반 응답 해석
- circuit breaker 또는 retry 정책 구현

이는 의도적인 범위 제한이며,  
포트폴리오에서는 구조 설명과 설계 의도를 우선했습니다.

---

## 9. Future Improvements

운영 환경을 더 가깝게 반영하려면 다음과 같은 보완이 가능합니다.

- Provider별 에러코드 표준화 매핑
- timeout / invalid response / provider unavailable 구분
- retryable / non-retryable 플래그 도입
- traceId 기반 요청 추적
- provider별 logging / monitoring 포인트 추가
- circuit breaker 또는 fallback 정책 검토

---

## 10. Summary

이 프로젝트에서 중요하게 본 오류 처리 포인트는 다음과 같습니다.

- 미지원 Provider 요청은 명확히 실패 처리
- 잘못된 요청은 Provider 호출 전에 차단
- Provider 응답 이상은 Backend 내부에서 우선 흡수
- timeout은 재시도 가능 여부와 함께 판단
- 클라이언트에는 공통 에러 응답 구조를 제공하는 방향 지향

이를 통해 `provider-integration-gateway`는  
단순한 분기 API가 아니라,  
다수 Provider 연동 환경에서 오류 처리 기준까지 고려한  
운영형 게이트웨이 구조를 설명하는 포트폴리오로 정리할 수 있습니다.

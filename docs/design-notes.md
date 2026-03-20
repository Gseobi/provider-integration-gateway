# Design Notes

## 1. Overview

`provider-integration-gateway`는  
다수의 결제대행사(PG)를 연동하는 환경에서  
결제 요청을 적절한 Provider로 분기하고,  
Provider별 요청 데이터 구성을 Backend 게이트웨이에서 책임지는 구조를 정리한 프로젝트입니다.

이 문서는 해당 구조를 선택한 이유와  
주요 설계 판단을 정리한 문서입니다.

---

## 2. Why a Provider Integration Gateway Was Needed

실제 운영 환경에서는 외국인 사용자가 주 고객인 모바일 애플리케이션 특성상,  
단일 통합 결제창 방식보다 결제수단별 PG사 웹페이지를 직접 호출하는 방식이 더 적합했습니다.

그 이유는 다음과 같았습니다.

- 통합 결제창 사용 시 한국어 UI 노출 비중이 높아 사용자 혼선 가능성 존재
- PG사별 로딩 화면, 인증 방식, UX 차이가 큼
- 결제수단별로 적절한 PG를 선택해야 하는 경우가 존재
- 앱 또는 클라이언트가 직접 PG 화면을 호출하더라도,  
  요청 검증과 Provider 선택 책임은 Backend가 가지는 것이 더 안정적임

이러한 환경에서는 단순 결제 API보다  
**PG 선택, 요청 데이터 구성, 응답 형식 정리 책임을 중앙에서 관리하는 게이트웨이 구조**가 필요하다고 보았습니다.

---

## 3. Why Provider Responsibility Was Separated

다수의 PG사를 연동할 때  
각 Provider는 요청 파라미터 형식, 필수값, 응답 해석 방식, 제약 조건이 다를 수 있습니다.

이 차이를 하나의 Service나 Controller 내부 조건문으로 계속 확장하면  
다음과 같은 문제가 발생할 수 있습니다.

- 분기 로직 증가
- Provider별 책임 혼합
- 신규 PG 추가 시 기존 코드 수정 범위 확대
- 테스트 범위 복잡도 증가

그래서 본 프로젝트에서는 Provider별 책임을 분리하는 방향을 선택했습니다.

- `PaymentController`: 요청 수신 및 응답 반환
- `PaymentService`: PG 선택 및 전체 흐름 조정
- `PaymentProviderStrategy`: Provider별 요청 데이터 구성 책임
- `Response DTO`: 클라이언트가 사용할 공통 응답 형태 유지

이렇게 나누면  
Provider별 차이는 Strategy 내부에 한정되고,  
상위 계층은 상대적으로 안정적인 구조를 유지할 수 있습니다.

---

## 4. Why Strategy Pattern Was Chosen

이 프로젝트에서는 PG 코드(KCP, INICIS, NICEPAY 등)를 기준으로  
적절한 Provider를 선택하는 구조가 필요했습니다.

이런 경우 Strategy Pattern은 다음 이유로 적합하다고 판단했습니다.

- 분기 대상을 명확하게 캡슐화 가능
- Provider별 동작을 독립적으로 구현 가능
- 신규 Provider 추가 시 기존 흐름 변경 최소화 가능
- 테스트 시 Provider 단위로 검증 가능

즉, Strategy Pattern은  
단순 디자인 패턴 적용 자체가 목적이 아니라,  
**Provider별 책임을 분리하고 확장 비용을 줄이기 위한 구조적 선택**이었습니다.

---

## 5. Why Backend Focuses on Request Data Instead of Rendering Payment Pages

실제 운영 환경에서는 PG별 결제 화면(WebView)을  
앱 또는 외부 클라이언트가 직접 호출하는 구조를 사용했습니다.

따라서 Backend의 핵심 책임은 다음과 같이 정리할 수 있었습니다.

- 어떤 PG를 사용할지 결정
- 요청값이 유효한지 검증
- 해당 PG가 요구하는 요청 데이터를 구성
- 클라이언트가 사용할 수 있는 형태로 응답 반환

반대로 Backend가 직접 화면을 렌더링하거나  
ModelAndView 기반으로 페이지를 반환하면 다음과 같은 부담이 생길 수 있습니다.

- PG별 UI 흐름과 Backend의 결합도 증가
- 화면/UI 변경 영향이 서버 구조에 전파
- 테스트 복잡도 증가
- 앱/WebView 구조와 역할 중복

그래서 본 프로젝트는  
**ModelAndView 기반 화면 반환 대신, 결제 요청에 필요한 데이터를 JSON 형태로 반환하는 방향**을 선택했습니다.

이를 통해 Backend의 책임을  
라우팅, 검증, 데이터 구성에 집중시킬 수 있도록 했습니다.

---

## 6. Why a Standardized Response Was Important

실제 PG 연동에서는 Provider마다  
요청 파라미터와 응답 형식이 달라질 수 있습니다.

그러나 클라이언트 입장에서는  
가능한 한 일관된 응답 구조를 받는 편이 처리 부담이 줄어듭니다.

그래서 본 프로젝트에서는  
Provider 내부 구현은 분리하되,  
외부로 노출되는 응답은 가능한 한 공통 Response DTO 중심으로 유지하는 방향을 기준으로 했습니다.

이 구조의 장점은 다음과 같습니다.

- 클라이언트 처리 방식 단순화
- Provider 변경 시 클라이언트 영향 최소화
- 공통 에러 응답 구조 확장 용이
- Backend 내부 Provider 차이를 외부에 직접 노출하지 않음

즉, 표준화의 목적은  
모든 Provider를 동일하게 만드는 것이 아니라,  
**차이를 Backend 내부에서 흡수하고 외부 계약을 안정적으로 유지하는 것**입니다.

---

## 7. Why This Project Was Reconstructed with Mock Providers

이 프로젝트는 실제 운영 경험을 바탕으로 했지만,  
포트폴리오에서는 보안 및 계약상 제약을 반드시 고려해야 했습니다.

실제 PG 연동에는 일반적으로 다음과 같은 민감한 요소가 포함됩니다.

- 연동 URL
- 상점 키 / 인증 키
- 서명 규칙
- 검증 파라미터
- 운영사별 계약 정보

이러한 정보는 포트폴리오에 포함할 수 없으므로,  
본 프로젝트에서는 실제 연동 자체보다 **구조와 책임 분리**를 보여주는 데 초점을 맞추고  
Mock Provider 기반으로 재구성했습니다.

이를 통해 다음을 유지하려 했습니다.

- 실무 설계 의도 반영
- PG 분기 구조 재현
- 요청/응답 책임 분리
- 확장 가능성 설명
- 보안 및 계약상 민감 정보 제거

즉, 이 프로젝트는 실제 결제 연동 구현 예제가 아니라,  
**운영형 결제 게이트웨이 구조를 설명하기 위한 설계형 포트폴리오**입니다.

---

## 8. Extension Consideration

이 구조는 신규 PG 추가를 고려해 설계했습니다.

새 Provider를 추가할 때 기대한 흐름은 다음과 같습니다.

1. PG enum 또는 식별값 추가
2. 신규 Provider Strategy 구현
3. 요청 데이터 구성 로직 추가
4. 필요 시 응답 매핑 확장

핵심은 상위 계층의 변경을 최소화하는 것입니다.  
즉, Controller나 전체 흐름을 매번 수정하기보다  
Provider 구현 단위에서 확장 가능한 구조를 지향했습니다.

---

## 9. Summary

이 프로젝트의 핵심 설계 방향은 다음과 같습니다.

- PG 선택과 요청 데이터 구성을 Backend 게이트웨이에서 중앙 관리
- Provider별 차이를 Strategy Pattern으로 분리
- Backend는 화면 반환보다 라우팅·검증·데이터 구성에 집중
- 공통 Response DTO를 통해 외부 계약 일관성 유지
- Mock 기반으로 보안 및 계약 제약을 제거하면서 구조를 재현

이를 통해 `provider-integration-gateway`는  
단순한 결제 API 예제가 아니라,  
다수 Provider 연동 환경에서 책임 분리와 확장성을 고려한  
운영형 게이트웨이 구조를 정리한 포트폴리오로 구성되었습니다.

# provider-integration-gateway

다수의 결제대행사(PG)를 연동하는 결제 요청 분기 게이트웨이 구조를  
포트폴리오 목적에 맞게 재구성한 프로젝트입니다.

실제 운영 환경에서 사용했던 PG 분기 전략, 책임 분리 구조, 결제 요청 흐름을 바탕으로  
보안 및 계약 제약을 고려해 **Mock 기반 구조**로 구현했습니다.

---

## 1. Project Overview

이 프로젝트는 다수의 PG사를 연동하는 환경에서  
결제 요청을 적절한 Provider로 분기하고,  
Provider별 요청 데이터 구성을 Backend 게이트웨이에서 책임지는 구조를 재구성한 포트폴리오 프로젝트입니다.

실제 운영 환경에서는 외국인 사용자가 주 고객인 모바일 애플리케이션 특성상,  
단일 통합 결제창 방식이 아닌 **결제수단별 PG사 웹페이지 직접 호출 방식**을 사용했습니다.

이 구조에서 Backend는 단순 API 제공을 넘어서 다음 역할을 담당했습니다.

- 결제 요청 검증
- PG 선택 및 분기
- PG별 요청 데이터 구성
- 앱 또는 클라이언트가 사용할 응답 데이터 반환

본 프로젝트는 위와 같은 실무 설계 경험을 바탕으로  
PG 선택 / 분기 / 요청 데이터 구성 책임을 Backend 게이트웨이로 명확히 분리한 구조를 Mock 기반으로 재현합니다.

---

## 2. Why This Project

실제 서비스에서는 외국인 사용자 대상이라는 특성상  
통합 결제창 사용 시 한국어 UI 노출 비중이 높고,  
PG사별 로딩 화면, 인증 방식, UX 차이가 커서 사용자 경험 측면의 제약이 있었습니다.

이에 따라 Backend에서 PG를 분기하고,  
클라이언트는 Backend가 구성한 요청 데이터를 기반으로 각 PG 결제 흐름을 호출하는 구조가 필요했습니다.

이 프로젝트는 다음과 같은 설계 의도를 중심으로 구성했습니다.

- PG 선택 책임을 Backend에 집중
- Provider별 요청/응답 책임 분리
- 클라이언트와 Provider 간 결합도 완화
- 신규 PG 추가 시 확장 가능한 구조 확보
- 보안 및 계약 제약을 고려한 Mock 기반 재현

---

## 3. Key Design Points

- **PG 분기 게이트웨이 구조**
  - PG 코드(KCP, INICIS, NICEPAY 등)를 기준으로 전략 분기
  - Provider별 책임을 분리된 Strategy로 관리

- **결제 요청 데이터 중심 설계**
  - 실제 PG 화면 호출은 앱 또는 외부 클라이언트가 담당
  - Backend는 결제 요청에 필요한 데이터 구성 및 반환에 집중

- **응답 구조 분리**
  - Provider별 응답 차이를 Backend에서 흡수
  - 클라이언트는 공통된 응답 형태로 처리 가능

- **확장 가능 구조**
  - 신규 PG 추가 시 Controller 수정 없이 Provider Strategy 추가 중심으로 확장 가능

- **Mock 기반 구현**
  - 실제 연동 URL, 인증 키, 서명 로직은 포함하지 않음
  - 구조 이해와 책임 분리를 목적으로 Mock 응답으로 재구성

---

## 4. Supported Flow

본 프로젝트는 아래와 같은 흐름을 기준으로 구성했습니다.

1. Client(App)가 결제 요청을 전송합니다.
2. Backend는 요청값을 검증합니다.
3. PaymentService가 PG 코드 기준으로 적절한 Provider Strategy를 선택합니다.
4. 선택된 Provider가 해당 PG 요청 데이터를 구성합니다.
5. Backend는 표준화된 Response DTO 형태로 결과를 반환합니다.
6. Client는 반환받은 데이터를 바탕으로 실제 결제 화면 호출 또는 후속 흐름을 처리합니다.

---

## 5. Architecture

1. Client(App)
2. PaymentController
3. PaymentService
4. PaymentProviderStrategy
   - KcpProvider (Mock)
   - InicisProvider (Mock)
   - NicepayProvider (Mock)
5. Response DTO

---

## 6. Request / Response Strategy

### Why ModelAndView Was Not Used

실제 운영 환경에서는 PG별 결제 화면(WebView)을  
앱에서 직접 호출하는 구조를 사용했습니다.

따라서 본 포트폴리오 프로젝트에서는 다음 방향을 선택했습니다.

- ModelAndView 기반 화면 반환 ❌
- 결제 요청에 필요한 데이터(JSON 형태) 반환 ⭕

이를 통해 다음을 의도했습니다.

- PG별 화면/UI 의존 제거
- Backend 책임을 라우팅·검증·데이터 구성으로 한정
- 테스트 용이성 확보
- Provider 추가 시 확장성 확보

### Response Direction

Provider별로 실제 응답 형식은 다를 수 있지만,  
클라이언트 입장에서는 가능한 한 일관된 방식으로 처리할 수 있도록  
Backend에서 공통 Response DTO 중심 구조를 유지하는 방향을 기준으로 했습니다.

---

## 7. Security and Constraints

- 실제 PG 연동 URL, 인증 키, 서명 규칙은 포함하지 않습니다.
- 계약 및 보안 이슈를 고려하여 외부 연동은 Mock으로 대체했습니다.
- 본 프로젝트는 실제 결제 승인 구현이 아니라, 구조와 책임 분리를 설명하기 위한 포트폴리오입니다.

---

## 8. Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **REST API**
- **Strategy Pattern**
- **Mock Provider**
- **Validation / DTO 기반 요청 모델링**

---

## 9. Test and Verification

본 프로젝트에서 확인하고자 하는 주요 시나리오는 다음과 같습니다.

- PG 코드 기준 Provider 분기 정상 동작
- Provider별 요청 데이터 구성 분리
- 공통 Response DTO 반환 구조 확인
- 지원하지 않는 PG 요청 처리
- Controller 수정 없이 Provider 확장 가능한 구조 확인

상세 검증 내용은 별도 문서로 정리할 예정입니다.

---

## 10. Future Improvements

- Provider별 에러 응답 표준화
- timeout / invalid response / retry 가능 여부 구분
- Provider registry 또는 factory 구조 보강
- 요청 서명 / 검증 단계 추상화
- 운영 로그 / 추적 필드 구조 보강

---

## 11. Documents

- [Design Notes](docs/design-notes.md)
- [Test Report](docs/test-report.md)
- [Error Handling Notes](docs/error-handling.md)

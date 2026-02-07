# provider-integration-gateway

다수의 결제대행사(PG)를 연동하는 결제 요청 분기 게이트웨이 구조를
포트폴리오 목적에 맞게 재구성한 프로젝트입니다.

실제 운영 환경에서 사용하던 PG 분기 전략, 책임 분리 구조, 결제 요청 흐름을
보안 및 계약 제약을 고려하여 Mock 기반으로 구현했습니다.

---

## 프로젝트 배경 (실무 기반 설계 의도)

실제 운영 환경에서는 외국인 사용자가 주 고객인 모바일 애플리케이션 특성상,
단일 통합 결제창 방식이 아닌 결제수단별 PG 결제사 웹페이지 직접 호출 방식을 사용했습니다.

이러한 구조를 선택한 이유는 다음과 같습니다.

- **통합 결제창 사용 시**: 한국어 UI 노출 비중이 높아 외국인 사용자에게 혼선 발생

- **PG사별 로딩 화면, 인증 방식, UX 차이가 큼**: Backend 단에서 PG를 분기하고, 결제 화면은 모든 PG에서 자체 개발한 통합 Loading WebView를 사용

- **앱 → Backend → PG 구조에서**: Backend는 결제 라우팅 및 검증 책임, WebView Open / Close 명령 담당

이 프로젝트는 위와 같은 실무 설계를 바탕으로,
PG 선택 / 분기 / 요청 데이터 구성 책임을 Backend 게이트웨이로 명확히 분리한 구조를 재현합니다.

---

## 주요 설계 포인트

- **PG 분기 게이트웨이 구조**
  - PG 코드(KCP, INICIS, NICEPAY 등)를 기준으로 전략 분기 (enum 기반 관리)
  - 각 PG별 요청/응답 책임을 분리된 Service로 관리
- 결제 요청 데이터 중심 설계
  - 실제 PG 화면 호출은 앱 또는 외부 클라이언트가 담당
  - Backend는 결제 요청에 필요한 데이터 구성 및 반환에 집중
- Mock 기반 구현
  - 실제 PG 연동 URL, KEY, 서명 로직은 포함하지 않음
  - 계약/보안 이슈를 고려하여 Mock 응답으로 구조만 재현
- 확장 가능 구조
  - PG 추가 시 Controller 수정 없이 Provider Strategy 추가만으로 확장 가능
 
---

## 아키텍처 개요

1. Client(App)
2. PaymentController
3. PaymentService (PG 분기)
4. PaymentProviderStrategy
  - KcpProvider (Mock)
  - InicisProvider (Mock)
  - NicepayProvider (Mock)
5. Response DTO

---

## 반환 방식에 대한 설명 (ModelAndView 미사용)

실제 운영 환경에서는 PG별 결제 화면(WebView)을
앱에서 직접 호출하는 구조를 사용했습니다.

따라서 본 포트폴리오 프로젝트에서는:

- ModelAndView 기반 화면 반환 ❌
- 결제 요청에 필요한 데이터(JSON 형태) 반환 ⭕
으로 구성했습니다.

이를 통해:
- PG별 화면/UI 의존 제거
- Backend의 책임을 라우팅·검증·데이터 구성으로 한정
- 테스트 및 확장 용이성 확보
를 목표로 설계했습니다.

---

## 사용 기술

- Java 17
- Spring Boot 3.x
- REST API
- Strategy Pattern 기반 PG 분기
- Mock Provider 구현
- Validation / DTO 기반 요청 모델링

---

## 보안 및 제약 사항

- 실제 PG 연동 URL, 인증 키, 서명 규칙은 포함하지 않습니다.
- 계약 및 보안 이슈를 고려하여 모든 외부 연동은 Mock으로 대체했습니다.
- 설계 의도와 구조 이해를 목적으로 한 포트폴리오입니다.

---

> 요약
> 외국인 사용자 대상 서비스에서 PG별 UX 차이를 고려해
> Backend는 결제 분기와 데이터 구성만 담당하도록 설계했던
> 실무 경험을 포트폴리오용으로 재구성한 프로젝트입니다.

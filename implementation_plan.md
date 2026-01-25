# 구현 계획서 (Implementation Plan)

## 1. 기술 스택 요약 (Tech Stack Summary)

| 구분 | 기술 선정 | 버전/상세 |
| :--- | :--- | :--- |
| **Backend** | **Spring Boot** | 3.x (Java 17+) |
| **Database** | **MySQL** | 8.0+ |
| **Frontend** | **Flutter** | Latest Stable (Dart) |
| **Infra** | **Local Environment** | Docker (DB), Localhost |

## 2. 상세 아키텍처 (Detailed Architecture)

### 2.1. Backend (Server)
- **Framework:** Spring Boot Web (REST API)
- **ORM:** Spring Data JPA (Hibernate)
- **Auth:** Spring Security (OAuth2 - Google) + JWT
- **Build Tool:** Gradle (Groovy/Kotlin DSL)
- **Role:**
    - 사용자 인증/인가 및 세션 관리.
    - 아이템 데이터 및 뽑기 확률 로직(Weight Table) 처리.
    - 걸음 수 검증 및 보상 지급 트랜잭션 관리.

### 2.2. Frontend (Mobile App)
- **Framework:** Flutter
- **Language:** Dart
- **Libraries/Packages:**
    - `pedometer` / `sensors_plus`: 만보기 센서 스트림 처리.
    - `home_widget` (or `android_alarm_manager_plus`): 홈 화면 위젯 구현.
    - `provider` or `riverpod`: 상태 관리.
    - `dio` or `http`: REST API 통신.
    - `flame` (Optional): 복잡한 게임 루프나 애니메이션이 필요할 경우 게임 엔진 도입.

### 2.3. Database (RDB)
- **DBMS:** MySQL
- **Schema Design:**
    - `Users`: 계정 정보, 재화(Coin, Footprints).
    - `Items`: 전체 아이템 메타데이터 (등급, 이미지 경로 등).
    - `Inventory`: 사용자가 보유한 아이템 매핑.
    - `DailyStats`: 일별 걸음 수 기록.

## 3. 개발 워크플로우 (Workflow)
1.  **Backend Init:** Spring Boot 프로젝트 생성 및 DB 스키마 설계.
2.  **API Dev:** 로그인(Google OAuth), 정보 조회, 뽑기 API 구현.
3.  **App Init:** Flutter 프로젝트 생성 및 기본 UI(도로롱, 만보기) 구현.
4.  **Integration:** 앱-서버 연동 (API 호출).
5.  **Feature Dev:** 위젯 및 백그라운드 센서(Service) 최적화.

## 4. 인프라 (Deployment): 로컬 우선
초기 개발 단계에서는 비용/복잡도 절감을 위해 로컬 환경에서 시작하는 것을 권장합니다.

- **개발 환경:**
    - **Backend:** 로컬 PC (IntelliJ)
    - **DB:** 로컬 설치 또는 Docker 컨테이너
    - **App:** 본인 휴대폰 연결 또는 안드로이드 에뮬레이터

## 5. 기술 스택 매핑 (Tech Stack Mapping)
사용자의 요구사항과 기획 의도에 맞춰 기술 스택을 선정했습니다.

| 기획 요구사항 | 기술 선정 | 이유 |
| :--- | :--- | :--- |
| **백엔드 학습 (User Goal)** | **Spring Boot (Java)** | 사용자 희망 1순위. 데이터 로직(뽑기) 구현에 적합. |
| **데이터 저장 (Data)** | **MySQL** | Spring Boot와 가장 호환성이 좋고 표준적인 RDB. |
| **만보기/센서 (Pedometer)** | **Flutter (Dart)** | `pedometer` 패키지로 OS 센서 데이터 스트림 처리 용이. |
| **홈 위젯 (Widget)** | **Flutter** | `home_widget` 패키지로 네이티브 위젯 연동 지원. |
| **UI/Animation** | **Flutter** | Skia 엔진 기반의 고성능 렌더링으로 부드러운 캐릭터 모션 구현. |

## 6. 최종 제안 (Recommendation)
백엔드 로직 구현에 집중할 수 있도록, 프론트엔드 장벽을 낮추는 조합입니다.

| 구분 | 기술 스택 | 비고 |
| :--- | :--- | :--- |
| **Backend** | **Spring Boot (Java)** | 사용자 희망 |
| **Database** | **MySQL** | 가장 표준적인 조합 |
| **Frontend** | **Flutter (Dart)** | 최상의 앱 성능 및 Java 친화적 문법 |
| **Communication**| **REST API** | JSON 통신 |

## Todo

(각 단계를 마친 후에는 반드시 테스트를 최신화)

- [x] 1. 불필요한 Converter 제거하고 dto 클래스쪽 static 팩토리 메서드로 일원화
- [x] 2. 유틸리티 클래스들의 인스턴스화 방지
- [x] 3. 인증 로직 안정성 개선 (UseGuardsAspect, Lazy Loading, Accessor 패턴 적용)
- [x] 4. 테스트 코드 "상황 - 기대결과" 를 모두 점검하고, 논리적으로 문제가 있는 부분, 잠재적 오류 발생 가능성이 있는 연관관계 등 모두 점검 (분석 리포트 완료: `.agents/review/test-quality-report.md`)
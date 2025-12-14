package org.veri.be;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.context.MemberContext;

@SpringBootTest
class MemberContextTest extends IntegrationTestSupport {

  @Test
  void 현재_인증된_사용자_가져오기() {
    Member member = MemberContext.getMemberOrThrow();
    Assertions.assertNotNull(member.getId());

    Assertions.assertEquals(this.getMockMember().getEmail(), member.getEmail());
  }
}

package org.veri.be.global.auth.context;


import java.util.Optional;

public class AccessTokenMemberContext {

  private static final ThreadLocal<AccessTokenMember> currentTokenMember = new ThreadLocal<>();

  public static void setAccessTokenMember(AccessTokenMember member) {
    currentTokenMember.set(member);
  }

  public static Optional<AccessTokenMember> getAccessTokenMember() {
    return Optional.ofNullable(currentTokenMember.get());
  }

  public static void clear() {
    currentTokenMember.remove();
  }
}

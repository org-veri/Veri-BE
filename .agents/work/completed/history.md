2025-12-21 05:13:58 | Jwt Exception Handling Shift | Added TokenProvider wrapper to translate JWT parsing errors into **UnAuthorizedException** and removed JWT handler from **GlobalExceptionHandler**. | Modified Files: `src/main/java/org/veri/be/global/auth/token/JwtExceptionHandlingTokenProvider.java`, `src/main/java/org/veri/be/global/auth/token/TokenProviderConfig.java`, `src/main/java/org/veri/be/lib/exception/handler/GlobalExceptionHandler.java`
Modified Files:
```
src/main/java/org/veri/be/global/auth/token/JwtExceptionHandlingTokenProvider.java
src/main/java/org/veri/be/global/auth/token/TokenProviderConfig.java
src/main/java/org/veri/be/lib/exception/handler/GlobalExceptionHandler.java
```

## History
- **2025-12-21**: Migrated shared history log into **.agents/work** structure.

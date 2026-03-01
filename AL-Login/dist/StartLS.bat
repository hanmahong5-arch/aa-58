@ECHO off
:: Run in Windows Terminal if available
IF "%WT_SESSION%" == "" wt %0 2>nul && EXIT
TITLE Encom 5.8 - Login Server

:START
CLS
JAVA -Xms64m -Xmx256m -XX:+UseNUMA -XX:+UseCompactObjectHeaders ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  -cp "libs/*" com.aionemu.loginserver.LoginServer
IF %ERRORLEVEL% EQU 0 GOTO END
IF %ERRORLEVEL% EQU 2 GOTO START
ECHO.
ECHO Login server has terminated abnormally!
ECHO.
PAUSE >nul
EXIT

:END
ECHO.
ECHO Login server has shut down
ECHO.
PAUSE >nul
EXIT

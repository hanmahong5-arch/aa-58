@ECHO off
:: Run in Windows Terminal if available
IF "%WT_SESSION%" == "" wt %0 2>nul && EXIT
TITLE Encom 5.8 - Game Server

:START
CLS
JAVA -Xms2048m -Xmx8192m -XX:+UseNUMA -XX:+UseCompactObjectHeaders ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
  --add-opens java.base/java.io=ALL-UNNAMED ^
  -javaagent:libs/AL-Commons-5.8-SNAPSHOT.jar ^
  -cp "libs/*" com.aionemu.gameserver.GameServer
IF %ERRORLEVEL% EQU 0 GOTO END
IF %ERRORLEVEL% EQU 2 GOTO START
ECHO.
ECHO Game server has terminated abnormally!
ECHO.
PAUSE >nul
EXIT

:END
ECHO.
ECHO Game server has shut down
ECHO.
PAUSE >nul
EXIT

#!/bin/bash
#=====================================================================================
# Usage:        ./StartGS.sh [jvmArgs]
# Description:  Starts the game server and restarts it depending on returned exit code.
#=====================================================================================

loop() {
  while true; do
    java -Xms2048m -Xmx8192m -XX:+UseNUMA -XX:+UseCompactObjectHeaders \
      --add-opens java.base/java.lang=ALL-UNNAMED \
      --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
      --add-opens java.base/java.io=ALL-UNNAMED \
      -javaagent:libs/AL-Commons-5.8-SNAPSHOT.jar \
      $@ -cp "libs/*" com.aionemu.gameserver.GameServer
    err=$?
    case $err in
      0) # regular shutdown
        echo "Game server stopped."
        break
        ;;
      1) # critical config or build error
        >&2 echo "Game server stopped with a fatal error."
        break
        ;;
      2) # restart request
        echo "Restarting game server..."
        ;;
      130) # SIGINT / Ctrl+C
        echo "Game server process was stopped."
        break
        ;;
      137|143) # 137=SIGKILL, 143=SIGTERM
        echo "Game server process was killed."
        break
        ;;
      *) # other
        >&2 echo "Game server has terminated abnormally (code: $err), restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  exit $err
}

pid=$(jps -l | grep com.aionemu.gameserver.GameServer | awk '{print $1}')
if [[ -n $pid ]]; then
  echo "Game server is already running (PID $pid)"
  read -p "Shut it down? (y/n) " answer
    if [[ $answer =~ ^y(es)?$ ]]; then
      if [[ -n $MSYSTEM ]]; then
        /bin/kill -fW -SIGINT $pid
      else
        kill -SIGINT $pid
      fi
      echo "Sent stop signal"
      until ! (jps -q | grep ^$pid$ > /dev/null); do echo "Waiting for shutdown..."; sleep 1; done
    else
      echo "Aborting server start"
      exit 1
    fi
fi
cd "$(dirname "$(readlink -f "$0")")"
loop $@

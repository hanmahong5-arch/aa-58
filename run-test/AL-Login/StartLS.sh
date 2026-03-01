#!/bin/bash
#=====================================================================================
# Usage:        ./StartLS.sh [jvmArgs]
# Description:  Starts the login server and restarts it depending on returned exit code.
#=====================================================================================

loop() {
  while true; do
    java -Xms64m -Xmx256m -XX:+UseNUMA -XX:+UseCompactObjectHeaders \
      --add-opens java.base/java.lang=ALL-UNNAMED \
      $@ -cp "libs/*" com.aionemu.loginserver.LoginServer
    err=$?
    case $err in
      0) # regular shutdown
        echo "Login server stopped."
        break
        ;;
      1) # critical config or build error
        >&2 echo "Login server stopped with a fatal error."
        break
        ;;
      2) # restart request
        echo "Restarting login server..."
        ;;
      130) # SIGINT / Ctrl+C
        echo "Login server process was stopped."
        break
        ;;
      137|143) # 137=SIGKILL, 143=SIGTERM
        echo "Login server process was killed."
        break
        ;;
      *) # other
        >&2 echo "Login server has terminated abnormally (code: $err), restarting in 5 seconds."
        sleep 5
        ;;
    esac
  done
  exit $err
}

pid=$(jps -l | grep com.aionemu.loginserver.LoginServer | awk '{print $1}')
if [[ -n $pid ]]; then
  echo "Login server is already running (PID $pid)"
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

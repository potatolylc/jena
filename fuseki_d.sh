#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# =========
#
# Startup script for Fuseki under *nix systems (works with cygwin too)
#
# Configuration
# -------------
# Default values are loaded from /etc/default/fuseki, if it exists.
#
# JAVA
#   Command to invoke Java. If not set, java (from the PATH) will be used.
#
# JAVA_OPTIONS
#   Extra options to pass to the JVM.
#
# FUSEKI_HOME
#   Where Fuseki is installed.  If not set, the script will try
#   to guess it based on the script invokation path.
#
# FUSEKI_RUN
#   Where the fuseki.pid file should be stored.  It defaults
#   first available of /var/run, /usr/var/run, and /tmp if not set.
#
# FUSEKI_PID
#   The FUSEKI PID file, defaults to $FUSEKI_RUN/fuseki.pid
#
# FUSEKI_ARGS
#   The arguments to pass to the Fuseki server on the command line. Defaults to:
#    --update --loc=$FUSKEI_DATA_DIR /ds    # if FUSEKI_CONF is not set
#    --config=$FUSEKI_CONF                  # if FUSEKI_CONF is set
#
# FUSEKI_CONF
#   The Fuseki configuration file, usually in RDF Turtle notation.
#
# FUSEKI_USER
#   If set, the server will be run as this user
#
# FUSEKI_DATA_DIR
#   The location of the data directory Fuseki will use (i.e. the value of --loc).
#   Defaults to $FUSEKI_HOME/DB
#
# FUSEKI_LOGS
#   Directory where logs will be generated. Defaults to $FUSEKI_HOME/log
#
# FUSEKI_LOGS_STDERROUT
#   Log file with stderr and stdout log output from Fuseki. Defaults to
#   $FUSEKI_LOGS/stderrout.log

### BEGIN INIT INFO
# Provides:          fuseki
# Required-Start:    $remote_fs $network
# Required-Stop:     $remote_fs $network
# Default-Start:     3 4 5
# Default-Stop:      0 1 2 6
# Short-Description: Start Jena Fuseki at boot time
# Description:       Jena Fuseki is a service that provides a SPARQL API over HTTP to one more RDF triple stores
### END INIT INFO

# DEBUG=1
NAME=fuseki
if [ -f /etc/default/$NAME ]; then
  . /etc/default/$NAME
fi

if [ -f /lib/lsb/init-functions ]; then
  . /lib/lsb/init-functions
else
  # simple replacements for LSB daemon logging functions if not defined
  log_daemon_msg() {
    echo $1
  }
  log_begin_msg() {
    echo $1
  }
  log_end_msg() {
    if [ $1 -eq 0]; then
      echo '[OK]'
    else
      echo '[failed]'
    fi
  }
fi

usage()
{
  echo "Usage: ${0##*/} {start|stop|restart|status}"
  exit 1
}

[ $# -gt 0 ] || usage

# Utility functions

findDirectory()
{
  local L OP=$1
  shift
  for L in "$@"; do
    [ "$OP" "$L" ] || continue
    printf %s "$L"
    break
  done
}

findFile()
{
  local L F=$1
  shift
  for L in "$@"; do
    [ -f "${L}/${F}" ] || continue
    printf %s "${L}/${F}"
    break
  done
}

running()
{
  local PID=$(cat "$1" 2>/dev/null) || return 1
  ps -p "$PID" >/dev/null 2>&1
}

# Are we running in cygwin?
cygwin=false
case "`uname`" in
    CYGWIN*) cygwin=true;;
esac


# Set FUSKEI_HOME to the script invocation directory if it is not specified
if [ -z "$FUSEKI_HOME" ]
then
  SCRIPT="$0"
  # Catch common issue: script has been symlinked
  if [ -L "$SCRIPT" ]
  then
    SCRIPT="$(readlink "$0")"
    # If link is relative
    case "$SCRIPT" in
      /*) ;; # fine
      *) SCRIPT=$( dirname "$0" )/$SCRIPT;; # fix
    esac
  fi

  # Work out root from script location
  FUSEKI_HOME="$( cd "$( dirname "$SCRIPT" )" && pwd )"

fi

# Deal with more Cygwin path issues
if [ "$cygwin" == "true" ]
then
  FUSEKI_HOME=`cygpath -w "$FUSEKI_HOME"`
 fi

if [ ! -e "$FUSEKI_HOME" ]
then
  log_daemon_message "$FUSEKI_HOME does not exist" 1>&2
  exit 1
fi


# Find a location for the pid file
if [ -z "$FUSEKI_RUN" ]
then
  FUSEKI_RUN=$(findDirectory -w /var/run /usr/var/run $FUSEKI_HOME /tmp)
fi

# Get PID file name
if [ -z "$FUSEKI_PID" ]
then
  FUSEKI_PID="$FUSEKI_RUN/fuseki.pid"
fi

# Log directory
if [ -z "$FUSEKI_LOGS" ]
then
  FUSEKI_LOGS="$FUSEKI_HOME/log"
fi

# Std Err and Out log
if [ -z "$FUSEKI_LOGS_STDERROUT" ]
then
  FUSEKI_LOGS_STDERROUT="$FUSEKI_LOGS/stderrout.log"
fi

# Data directory
if [ -z "$FUSEKI_DATA_DIR" ]
then
  FUSEKI_DATA_DIR="$FUSEKI_HOME/DB"
fi

# Set up JAVA if not set
if [ -z "$JAVA" ]
then
  JAVA=$(which java)
fi
if [ -z "$JAVA" ]
then
  echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.6) in your PATH." 2>&2
  exit 1
fi

# The location of the start up JAR
FUSEKI_START=${FUSEKI_START:-$FUSEKI_HOME/fuseki-server.jar}

# Deal with Cygwin path issues
if [ "$cygwin" == "true" ]
then
  DATA_DIR=`cygpath -w "$FUSEKI_DATA_DIR"`
  FUSEKI_START=`cygpath -w "$FUSEKI_START"`
else
  DATA_DIR="$FUSEKI_DATA_DIR"
fi

# Some JVM settings
if [ -z "$JAVA_OPTIONS" ]
then
  JAVA_OPTIONS="-Xmx1200M"
fi


# Default Fuseki Arguments
if [ -z "$FUSEKI_ARGS" ]
then
  if [ -z "$FUSEKI_CONF" ]
  then
    FUSEKI_ARGS="--update --loc=$DATA_DIR /ds"
  else
    FUSEKI_ARGS="--config=$FUSEKI_CONF"
  fi
fi

# Run command

RUN_ARGS=(${JAVA_OPTIONS[@]} -jar "$FUSEKI_START" $FUSEKI_ARGS)
RUN_CMD=("$JAVA" ${RUN_ARGS[@]})


#####################################################
# Comment these out after you're happy with what
# the script is doing.
#####################################################
if (( DEBUG ))
then
  log_daemon_msg "FUSEKI_HOME    =  $FUSEKI_HOME"
  log_daemon_msg "FUSEKI_CONF    =  $FUSEKI_CONF"
  log_daemon_msg "FUSEKI_RUN     =  $FUSEKI_RUN"
  log_daemon_msg "FUSEKI_PID     =  $FUSEKI_PID"
  log_daemon_msg "FUSEKI_ARGS    =  $FUSEKI_ARGS"
  log_daemon_msg "FUSEKI_START   =  $FUSEKI_START"
  log_daemon_msg "CONFIGS        =  ${CONFIGS[*]}"
  log_daemon_msg "JAVA           =  $JAVA"
  log_daemon_msg "JAVA_OPTIONS   =  ${JAVA_OPTIONS[*]}"
  log_daemon_msg "RUN_ARGS       =  ${RUN_ARGS[@]}"
  log_daemon_msg "RUN_CMD        =  ${RUN_CMD[@]}"
fi

NO_START=0

# Life cycle functions
start() {
  if (( NO_START )); then
    log_daemon_msg "Not starting Fuseki - NO_START=1"
    exit
  fi

  # Make sure the data and log directories exist
  mkdir -p "$FUSEKI_DATA_DIR"
  mkdir -p "$FUSEKI_LOGS"

  # Make sure the .jar file exists
  if [ ! -e $FUSEKI_START ]; then
    log_daemon_msg "Could not see Fuseki .jar file: \$FUSEKI_START has value '$FUSEKI_START'"
    exit 1
  fi

  log_begin_msg "Starting Fuseki"
  if type start-stop-daemon > /dev/null 2>&1
  then
    unset CH_USER
    if [ -n "$FUSEKI_USER" ]
    then
      CH_USER="--chuid $FUSEKI_USER"
    fi
    if start-stop-daemon --start $CH_USER --chdir "$FUSEKI_HOME" --background --make-pidfile --pidfile "$FUSEKI_PID" --startas "$JAVA" -- "${RUN_ARGS[@]}"
    then
      sleep 2
      if running "$FUSEKI_PID"
      then
        log_end_msg 0
        print_started
      else
        log_end_msg 1
      fi
    else
      log_end_msg 1
      log_daemon_msg "** start-stop-daemon failed to run"
    fi
  else
    if running $FUSEKI_PID
    then
      log_end_msg 1
      log_daemon_msg 'Already Running!'
      exit 1
    else
      # dead pid file - remove
      rm -f "$FUSEKI_PID"
    fi

    if [ "$FUSEKI_USER" ]
    then
      touch "$FUSEKI_PID"
      chown "$FUSEKI_USER" "$FUSEKI_PID"
      su - "$FUSEKI_USER" -c "
        log_daemon_msg "Redirecting Fuseki stderr/stdout to $FUSEKI_LOGS_STDERROUT"
        exec ${RUN_CMD[*]} &
        disown \$!
        echo \$! > '$FUSEKI_PID'"
    else
      #log_daemon_msg "Redirecting Fuseki stderr/stdout to $FUSEKI_LOGS_STDERROUT"
      exec "${RUN_CMD[@]}" &> "$FUSEKI_LOGS_STDERROUT" &
      disown $!
      echo $! > "$FUSEKI_PID"
    fi

    log_end_msg 0
    print_started
  fi
}

print_started() {
  log_daemon_msg "STARTED Fuseki `date`"
  log_daemon_msg "PID=$(cat "$FUSEKI_PID" 2>/dev/null)"
}

delete_fuseki_pid_file() {
  rm -f "$FUSEKI_PID"
}

stop() {
  log_begin_msg "Stopping Fuseki: "

  if ! running "$FUSEKI_PID"
  then
    log_end_msg 1

    # if a stop rather than a restart, signal failure to stop
    if [ -z "$1" ]
    then
      exit 1
    fi
  fi

  ###############################################################
  # !!!! This code needs to be improved, too many repeats !!!!  #
  ###############################################################
  if type start-stop-daemon > /dev/null 2>&1; then
    start-stop-daemon --stop --pidfile "$FUSEKI_PID" --chdir "$FUSEKI_HOME" --startas "$JAVA" --signal HUP

    ## Die after a 30 second timeout
    TIMEOUT=30
    while running "$FUSEKI_PID"; do
      if (( TIMEOUT-- == 0 )); then
        start-stop-daemon --stop --pidfile "$FUSEKI_PID" --chdir "$FUSEKI_HOME" --startas "$JAVA" --signal KILL
      fi
        sleep 1
    done
    delete_fuseki_pid_file
    log_end_msg 0
  else
    PID=$(cat "$FUSEKI_PID" 2>/dev/null)
    kill "$PID" 2>/dev/null

    TIMEOUT=30
    while running $FUSEKI_PID; do
      if (( TIMEOUT-- == 0 )); then
        kill -KILL "$PID" 2>/dev/null
      fi
      sleep 1
    done
    delete_fuseki_pid_file
    log_end_msg 0
  fi
}

case $1 in
  start)
    start
  ;;
  stop)
    stop
  ;;
  restart)
    stop "restarting"
    start
  ;;
  status)
    FUSEKI_PID=$(findFile fuseki.pid /var/run /usr/var/run $FUSEKI_HOME /tmp)
    if running $FUSEKI_PID
    then
      PID=`cat "$FUSEKI_PID"`
      log_daemon_msg "Fuseki is running with pid: $PID"
    else
      log_daemon_msg "Fuseki is not running"
    fi
  ;;
  *)
    usage
  ;;
esac

exit 0

#!/usr/bin/env bash

# Start/stop a Flink JobManager.
USAGE="Usage: e2datamanager.sh (start|stop)"

STARTSTOP=$1

if [[ $STARTSTOP != "start" ]] && [[ $STARTSTOP != "stop" ]]; then
  echo $USAGE
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/config.sh

if [[ $STARTSTOP == "start" ]]; then
  "$FLINK_BIN_DIR"/start-cluster.sh
elif [[ $STARTSTOP == "stop" ]]; then
  "$FLINK_BIN_DIR"/stop-cluster.sh stop-all
fi

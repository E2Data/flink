#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/config.sh

# find E2DataServer pid
pid=`jps | grep E2DataServer | awk '{print $1}'`
#echo $pid

# Kill the process if pid isn't empty
[ ! -z $pid ] && kill -9 $pid

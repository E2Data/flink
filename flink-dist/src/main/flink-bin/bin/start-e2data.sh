#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/config.sh

# Start backend Netty server
JAVA_VERSION=$(${JAVA_RUN} -version 2>&1 | sed 's/.*version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')

# Only set JVM 8 arguments if we have correctly extracted the version
if [[ ${JAVA_VERSION} =~ ${IS_NUMBER} ]]; then
    if [ "$JAVA_VERSION" -lt 18 ]; then
        JVM_ARGS="$JVM_ARGS -XX:MaxPermSize=256m"
    fi
fi

FLINK_TM_CLASSPATH=`constructFlinkClassPath`
export FLINK_ENV_JAVA_OPTS="${FLINK_ENV_JAVA_OPTS} ${FLINK_ENV_JAVA_OPTS_HS}"
args=("--configDir" "${FLINK_CONF_DIR}")

#echo "version: $JAVA_VERSION, run: $JAVA_RUN, jvm args: $JVM_ARGS \n"
#echo ${args[@]}
#echo "`manglePathList "$FLINK_TM_CLASSPATH"`"

CLASS_TO_RUN=org.apache.flink.runtime.e2data.E2DataServer
$JAVA_RUN $JVM_ARGS -classpath "`manglePathList "$FLINK_TM_CLASSPATH"`" ${CLASS_TO_RUN} "${args[@]}"

## Start frontend app
#cd "/Users/christos/Projects/flink/flink-e2data-web/web-dashboard/"
#npm run e2data

#!/bin/bash
set -ex
set -o pipefail

## resolve folder of this script, following all symlinks,
## http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  [[ $SCRIPT_SOURCE != /* ]] && SCRIPT_SOURCE="$SCRIPT_DIR/$SCRIPT_SOURCE"
done
readonly SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"

source $SCRIPT_DIR/testlib.bash

parseArguments "$@"
processArguments
setup
echo "Verifier for Jira Ticket: OPENJDK-390." | tee $REPORT_FILE
echo "Jira Link: https://issues.redhat.com/browse/OPENJDK-390" | tee -a $REPORT_FILE
checkJavaHomeEnvExists 2>&1| tee -a $REPORT_FILE
checkJavaHomeEnvIsFile 2>&1| tee -a $REPORT_FILE
checkJavaHomeEnvVarValid 2>&1| tee -a $REPORT_FILE

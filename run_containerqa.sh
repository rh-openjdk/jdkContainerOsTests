#!/bin/bash

###############################################################################
##  run_containerqa.sh
##  This script sets up the host under test for running the
##  container testing against the Red Hat OpenJDK containers
##
###############################################################################

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

# Help function
function print_help() {
  echo "Name: run_containerqa.sh
       Description: Red Hat OpenJDK testsuite for Red Hat OpenShift Container Platform.
       Command-line arguements:
           --container-image
               Container image under test.
           --report-dir
               Directory to store the execution logs.
       Example: run_containerqa.sh \
                      --container-image=registry.access.redhat.com/ubi8/openjdk-8:1.17-1.1693366248 \
                      --report-dir=/mnt/log_dir/
        "
  exit 1
}

# The commandline should be  `run_containerqa.sh --container-image=registry.access.redhat.com/ubi8/openjdk-8:1.17-1.1693366248`
for a in "$@"
do
  case $a in
      --container-image=*)
        CONTAINER_VERSION="${a#*=}"
      ;;
      --report-dir=*)
        ARG_REPORT_DIR="${a#*=}"
      ;;
      *)
         echo "Unrecognized argument: '$a'" >&2
         print_help >&2
      ;;
  esac
done

if [ -z "$CONTAINER_VERSION" ] ; then
  print_help >&2
fi
if  [ -n "$ARG_REPORT_DIR" ] ; then
  if [ ! -e "$ARG_REPORT_DIR" ] ; then
    echo "$ARG_REPORT_DIR do not exists"
    exit 2
  fi
fi

echo "Ensure that the run-folder-as-tests sub-repo is properly pulled down."
echo "Ensure Podman is installed on the host under tests."
echo "See test.yaml file if in doubts"


TIME=$(date +%s)
jtWork="test.${TIME}/jdk/work"
jtReport="test.${TIME}/jdk/report"
mkdir -p $jtWork
mkdir -p $jtReport
export SCRATCH_DISK="`pwd`/$jtWork"
export WORKSPACE="`pwd`/$jtReport"
SUITE_FOLDER="containersQa"
REMOTE_NORMAL_CONTAINER=true bash  ${SCRIPT_DIR}/run-folder-as-tests/run-folder-as-tests.sh ${SCRIPT_DIR}/${SUITE_FOLDER} ${CONTAINER_VERSION}  | tee test.${TIME}/tests.log

toPack="${jtReport} test.${TIME}/tests.log"
if [ "x$JNI_PACK_WORK" == "xtrue" ] ; then
toPack="$toPack ${jtWork}";
fi
tar -czf test.${TIME}.tar.gz  $toPack || echo "Packing of results tarball failed"
if ! [ -f test.${TIME}/tests.log ] ; then
	echo "Missing tests.log!" 1>&2
	exit 1
fi

if  [ -n "$ARG_REPORT_DIR" ] ; then
  if [ -e "$ARG_REPORT_DIR" ] ; then
    mv -v test.${TIME}.* "$ARG_REPORT_DIR"
  fi
fi

# results should be in log, if not, it means suite was not run
grep -Eqi -e '^passed' -e '^(failed|error)' -e '^Ignored' test.${TIME}/tests.log || exit 2

if [ "x$CQA_FAIL" == "xtrue" ] ; then
  if grep -Eq -e '^Failed: [1-9]' test.${TIME}/tests.log  ; then
    exit 1
  fi
fi


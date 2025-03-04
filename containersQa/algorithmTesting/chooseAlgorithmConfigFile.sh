#!/bin/bash

function chooseAlgorithmConfigFile() {
  if [ "$#" -ne 2 ] ; then
    echo "Needs two arguments. First one should be the version of RHEL and second one should be 'true' or 'fips', if FIPS mode is on."
    exit 1
  fi

  if [ "$1" == "el.9" -o "$1" == "el.9z" ] ; then
    if [ "$2" == "true" -o "$2" == "fips" ] ; then
      echo "el9ConfigFips.txt"
    else
      echo "el9ConfigLegacy.txt"
    fi
  elif [ "$1" == "el.8z" -o  "$1" == "el.8" ] ; then
    if [ "$2" == "true" -o "$2" == "fips" ] ; then
      echo "el8ConfigFips.txt"
    else
      echo "el8ConfigLegacy.txt"
    fi
  else
    echo "Unknown/unsupported RHEL version was specified: $1"
    exit 1
  fi
}

chooseAlgorithmConfigFile "${1}" "${2}"

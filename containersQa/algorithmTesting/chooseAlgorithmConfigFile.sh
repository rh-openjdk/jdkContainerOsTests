#!/bin/bash

function chooseAlgorithmConfigFile() {
  if [ "$#" -ne 2 ] ; then
    echo "Needs two arguments. First one should be the version of RHEL and second one should be 'true' or 'fips', if FIPS mode is on."
    exit 1
  fi

  if [ "$1" == "el.7" -o "$1" == "el.7z" ] ; then
    if [ "$2" == "true" -o "$2" == "fips" ] ; then
      echo "el7ConfigFips.txt"
    else
      echo "el7ConfigLegacy.txt"
    fi
  elif [ "$1" == "el.8" -o "$1" == "el.8z" -o "$1" == "el.8z2" -o  "$1" == "el.8z4" -o  "$1" == "el.8z6" -o  "$1" == "el.8z8" ] ; then
    if [ "$2" == "true" -o "$2" == "fips" ] ; then
      echo "el8ConfigFips.txt"
    else
      echo "el8ConfigLegacy.txt"
    fi
  elif [ "$1" == "el.9" -o "$1" == "el.9z" -o "$1" == "el.9z2" ] ; then
    if [ "$2" == "true" -o "$2" == "fips" ] ; then
      echo "el9ConfigFips.txt"
    else
      echo "el9ConfigLegacy.txt"
    fi
  elif [ "$1" == "el.10" ] ; then
    if [ "$2" == "true" -o "$2" == "fips" ] ; then
      echo "el10ConfigFips.txt"
    else
      echo "el10ConfigLegacy.txt"
    fi
  elif [ "$1" == "f.41" ] ; then
    if [ "$2" == "true" -o "$2" == "fips" ] ; then
      echo "fedoraConfigFips.txt"
    else
      echo "fedoraConfigLegacy.txt"
    fi
  else
    echo "Unknown/unsupported RHEL version was specified: $1"
    exit 1
  fi
}

chooseAlgorithmConfigFile "${1}" "${2}"

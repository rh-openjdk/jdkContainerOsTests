#!/bin/bash

function chooseAlgorithmConfigFile() {
  if [ "x$assume_fips" == "x" ] ; then
    echo "Need to set the variable assume_fips to either true or false."
    exit 1
  fi

  if [ "$OTOOL_OS" == "el.9" -o "$OTOOL_OS" == "el.9z" ] ; then
    if [ "$assume_fips" == "true" ] ; then
      echo el9ConfigFips.txt
    else
      echo el9ConfigLegacy.txt
    fi
  elif [ "$OTOOL_OS" == "el.8z" -o  "$OTOOL_OS" == "el.8" ] ; then
    if [ "$assume_fips" == "true" ] ; then
      echo el8ConfigFips.txt
    else
      echo el8ConfigLegacy.txt
    fi
  else
    echo "OTOOL_OS is not declared or unknown: $OTOOL_OS"
    exit 1
  fi
}

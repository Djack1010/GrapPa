#!/bin/bash
SCRIPTPATH=$PWD

if [ -f  $SCRIPTPATH/../../graphs/clean.sh ]; then
    $SCRIPTPATH/../../graphs/clean.sh all
else
    echo "ERROR! clean.sh not found..."
    exit
fi

rm -rf $SCRIPTPATH/sootOutput
exit

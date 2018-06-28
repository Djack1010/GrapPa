#!/bin/bash
SCRIPTPATH=$PWD

if [ ! -z $1 ]; then
    if [ -d "$SCRIPTPATH/../extTool" ]; then
        cd $SCRIPTPATH/../extTool
        for FoldExt in *; do
            if [ -d "${FoldExt}" ]; then
                if [ -f $FoldExt/clean.sh ]; then
                    echo "Running $FoldExt/clean.sh"
                    $FoldExt/clean.sh
                fi
            fi
        done
    fi
fi

if [ -d  $SCRIPTPATH/../../extTool ]; then
    for f in $SCRIPTPATH/../../extTool/*; do
            if [ -d "${f}" ]; then
                if [ -f $f/clean.sh ]; then
                    echo "Running $f/clean.sh"
                    $f/clean.sh
                fi
            fi
        done
fi

if [ -f  $SCRIPTPATH/../../graphs/clean.sh ]; then
    $SCRIPTPATH/../../graphs/clean.sh
fi


rm -rf $SCRIPTPATH/sootOutput
exit

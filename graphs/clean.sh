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

echo "Running $SCRIPTPATH/clean.sh"
cd $SCRIPTPATH
for Fold in $SCRIPTPATH/*; do
    if [ "${Fold}" == "$SCRIPTPATH/JimpleCode" ]; then
        cd $Fold
        for txtFile in $Fold/*.txt; do
            if [ -f $txtFile ]; then
                echo "Removing $txtFile"
                rm $txtFile
            fi
        done
    elif [ -d "${Fold}" ]; then
        cd $Fold
        for dotFile in $Fold/*.dot; do
            if [ -f $dotFile ]; then
                echo "Removing $dotFile"
                rm $dotFile
            fi
        done
        for pdfFile in $Fold/*.pdf; do
            if [ -f $pdfFile ]; then
                echo "Removing $pdfFile"
                rm $pdfFile
            fi
        done
    fi
done
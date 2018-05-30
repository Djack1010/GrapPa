#!/bin/bash
SCRIPTPATH=$PWD

for Fold in $SCRIPTPATH/*; do
    if [ -d "${Fold}" ]; then
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
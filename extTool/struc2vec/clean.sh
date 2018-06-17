#!/bin/bash
SCRIPTLOC="$( cd "$(dirname "$0")" ; pwd -P )"

for Fold in $SCRIPTLOC/*; do
    if [ -d "${Fold}" ]; then
        cd $Fold
        for edgeList in $Fold/*.edgelist; do
            if [ -f $edgeList ]; then
                echo "Removing $edgeList"
                rm $edgeList
            fi
        done
        for emb in $Fold/*.emb; do
            if [ -f $emb ]; then
                echo "Removing $emb"
                rm $emb
            fi
        done
        for text in $Fold/*.txt; do
            if [ -f $text ]; then
                echo "Removing $text"
                rm $text
            fi
        done
    fi
done
#!/bin/bash
SCRIPTPATH=$PWD

for Fold in $SCRIPTPATH/*; do
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
    fi
done
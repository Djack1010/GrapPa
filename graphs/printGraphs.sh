#!/bin/bash
SCRIPTPATH=$PWD

for Fold in $SCRIPTPATH/*; do
    if [ -d "${Fold}" ]; then
        cd $Fold
        for dotFile in $Fold/*.dot; do
            if [ -f $dotFile ]; then
                OUTPUT=$(echo $dotFile | cut -d'.' -f1)
                if [ -f $OUTPUT.pdf ]; then
                    echo "Graph for $dotFile already exist!"
                    dot -Tpdf $dotFile -o $OUTPUT.pdf
                else
                    echo "Generating graph for $dotFile"
                    dot -Tpdf $dotFile -o $OUTPUT.pdf
                fi
            fi
        done
    fi
done
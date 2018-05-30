#!/bin/bash
SCRIPTPATH=$PWD

rm -f $SCRIPTPATH/stats.txt

for dotFile in $SCRIPTPATH/3/*.dot; do
    if [ -f $dotFile ]; then
        LINEDOT=$(wc -l < $dotFile)
        codFile=$(echo $dotFile | cut -d'.' -f1 | sed 's/graphs\/3/graphs\/JimpleCode/g')
        codFile=$codFile".txt"
        if [ -f $codFile ]; then
            LINECOD=$(wc -l < $codFile)
            echo "$LINEDOT,$LINECOD,$dotFile" >> $SCRIPTPATH/stats.txt
        else
            echo "ERROR, $codFile not found, exiting..."
            exit
        fi
    fi
done

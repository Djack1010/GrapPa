#!/bin/bash
SCRIPTPATH=$PWD

rm -f $SCRIPTPATH/stats.txt

echo -e "Graph,Method,Name" >> $SCRIPTPATH/stats.txt
echo -e "Graph,Method" >> $SCRIPTPATH/statsPublic.txt

for dotFile in $SCRIPTPATH/3/*.dot; do
    if [ -f $dotFile ]; then
        LINEDOT=$(wc -l < $dotFile)
        codFile=$(echo $dotFile | cut -d'.' -f1 | sed 's/graphs\/3/graphs\/JimpleCode/g')
        codFile=$codFile".txt"
        if [ -f $codFile ]; then
            LINECOD=$(wc -l < $codFile)
            echo -e "$LINEDOT,$LINECOD,$dotFile" >> $SCRIPTPATH/stats.txt
            echo -e "$LINEDOT,$LINECOD" >> $SCRIPTPATH/statsPublic.txt
        else
            echo "ERROR, $codFile not found, exiting..."
            exit
        fi
    fi
done

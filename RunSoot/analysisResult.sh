#!/bin/bash
SCRIPTPATH=$PWD

rm -f $SCRIPTPATH/temp_result.txt

if [ -z $1 ]; then
    RESULT_PATH= $SCRIPTPATH/result.txt
else
    RESULT_PATH= $1
fi

if [ -f $RESULT_PATH ]; then
    while read l ;
    do
        #if [[ $l = "" ]] || [[ $l = "Warning"* ]] || [[ $l = "Writing"* ]] || [[ $l = "Transforming"* ]] || [[ $l = "No main class given."* ]] || [[ $l = "[Call Graph]"* ]] || [[ $l = "Soot "* ]]; then
        #    continue
        #else
        #    echo $l >> $SCRIPTPATH/temp_result.txt
        #fi
        if [[ $l = "RESULT "* ]]; then
            echo $l >> $SCRIPTPATH/temp_result.txt
        else
            continue
        fi
    done < $SCRIPTPATH/result.txt
    rm $SCRIPTPATH/result.txt
    cat $SCRIPTPATH/temp_result.txt >> result.txt
    rm $SCRIPTPATH/temp_result.txt
else
    echo "ERROR! result.txt not found, exiting..."
    exit
fi
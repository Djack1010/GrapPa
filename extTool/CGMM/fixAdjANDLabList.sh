#!/bin/bash
#Created by Giacomo Iadarola
#v1.0 - 17/06/18

SCRIPTLOC="$( cd "$(dirname "$0")" ; pwd -P )"
rm -f $SCRIPTLOC/temp

for f in $(find $SCRIPTLOC -name "*.adjlist" ); do 
    COUNT=S
    ERR=0
    while read l; do
        if [ "$COUNT" == "S" ]; then
            echo $l >> $SCRIPTLOC/temp
            COUNT=0
        elif [ "$(echo $l | cut -d' ' -f1)" == "$COUNT" ]; then
            echo $l >> $SCRIPTLOC/temp
            COUNT=$(($COUNT+1))
        else
            ERR=1
            while [ "$(echo $l | cut -d' ' -f1)" != "$COUNT" ]; do
                echo "$COUNT "  >> $SCRIPTLOC/temp
                COUNT=$(($COUNT+1))
            done
            echo $l >> $SCRIPTLOC/temp
            COUNT=$(($COUNT+1))
        fi
    done < $f
    rm $f
    mv $SCRIPTLOC/temp $f
    echo "Fixed $f - ERROR ($ERR)"
done

for f in $(find $SCRIPTLOC -name "*.txt" ); do 
    if [ $(echo $f | grep "labels2-") ]; then
        COUNT=S
        ERR=0
        while read l; do
            if [ "$COUNT" == "S" ]; then
                echo $l >> $SCRIPTLOC/temp
                COUNT=0
            elif [ "$(echo $l | cut -d' ' -f1)" == "$COUNT" ]; then
                echo $l >> $SCRIPTLOC/temp
                COUNT=$(($COUNT+1))
            else
                ERR=1
                while [ "$(echo $l | cut -d' ' -f1)" != "$COUNT" ]; do
                    echo "$COUNT 259"  >> $SCRIPTLOC/temp
                    COUNT=$(($COUNT+1))
                done
                echo $l >> $SCRIPTLOC/temp
                COUNT=$(($COUNT+1))
            fi
        done < $f
        rm $f
        mv $SCRIPTLOC/temp $f
        echo "Fixed $f - ERROR ($ERR)"
    else
        COUNT=S
        ERR=0
        while read l; do
            if [ "$COUNT" == "S" ]; then
                echo $l >> $SCRIPTLOC/temp
                COUNT=0
            elif [ "$(echo $l | cut -d' ' -f1)" == "$COUNT" ]; then
                echo $l >> $SCRIPTLOC/temp
                COUNT=$(($COUNT+1))
            else
                ERR=1
                while [ "$(echo $l | cut -d' ' -f1)" != "$COUNT" ]; do
                    echo "$COUNT 18"  >> $SCRIPTLOC/temp
                    COUNT=$(($COUNT+1))
                done
                echo $l >> $SCRIPTLOC/temp
                COUNT=$(($COUNT+1))
            fi
        done < $f
        rm $f
        mv $SCRIPTLOC/temp $f
        echo "Fixed $f - ERROR ($ERR)"
    fi
done

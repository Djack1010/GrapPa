#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
if [ -z "$1" ]; then
    SPER=90
else
    SPER=$1
fi
NUM=0
for l in $(cat $SCRIPTPATH/CounterLIT | cut -d'_' -f3 ); do
    NUM=$(($NUM+$l))
done
NUM=$(bc <<< "scale = 2; ($NUM / 100) * $SPER ")
NUM=$(echo $NUM | cut -d'.' -f1)
TOP=0
while read l; do
    NUM=$(($NUM-$(echo $l | cut -d'_' -f3 )))
    if [ "$NUM" -le "0" ]; then
        break
    else
        TOP=$(($TOP+1))
    fi
done < $SCRIPTPATH/CounterLIT
echo "LIT: Coverage ${SPER}% with first $TOP labels out of $(cat $SCRIPTPATH/CounterLIT | wc -l)"
NUM=0
for l in $(cat $SCRIPTPATH/CounterIDE | cut -d'_' -f3 ); do
    NUM=$(($NUM+$l))
done
NUM=$(bc <<< "scale = 2; ($NUM / 100) * $SPER ")
NUM=$(echo $NUM | cut -d'.' -f1)
TOP=0
while read l; do
    NUM=$(($NUM-$(echo $l | cut -d'_' -f3 )))
    if [ "$NUM" -le "0" ]; then
        break
    else
        TOP=$(($TOP+1))
    fi
done < $SCRIPTPATH/CounterIDE
echo "IDE: Coverage ${SPER}% with first $TOP labels out of $(cat $SCRIPTPATH/CounterIDE | wc -l)"
exit
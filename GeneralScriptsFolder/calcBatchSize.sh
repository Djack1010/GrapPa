#!/bin/bash

if [ -z "$1" ]; then
    echo "ERROR"
    exit
fi
TOT=0
for f in $1/*.adjlist; do
    VERT=$(cat $f | wc -l)
    TOT=$(($TOT+$VERT-1))
done

TOT=$(($TOT-2))

if [ $TOT -ge 2000 ]; then
    echo "2000"
else
    echo $TOT
fi
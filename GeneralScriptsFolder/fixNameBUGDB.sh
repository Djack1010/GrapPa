#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
IND=0
for nedoFile in $(find $SCRIPTPATH/mutated -name "*.nedo"); do
    TEMPNAME=${nedoFile##*/}
    FIRSTPART=$(echo $TEMPNAME | cut -d':' -f1)
    SECONDPART=$(echo $TEMPNAME | cut -d':' -f2)
    NEWNAME="$FIRSTPART:$IND:$SECONDPART"
    FIRSTLINE=$(head -n1 $nedoFile)
    NUMNOD=$(echo $FIRSTLINE | cut -d' ' -f2)
    sed -i "1s/.*/${NEWNAME} ${NUMNOD}/" $nedoFile
    mv $nedoFile $SCRIPTPATH/mutated/$NEWNAME
    IND=$(($IND+1))
    #exit
done
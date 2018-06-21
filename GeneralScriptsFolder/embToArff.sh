#!/bin/bash
SCRIPTPATH=$PWD

mkdir -p $SCRIPTPATH/arffFile

for embFile in $( find $SCRIPTPATH -name "*.emb") ; do
    ARFF="$( echo ${embFile##*/} | cut -d'.' -f1 ).arff"
    FILEARFF="$SCRIPTPATH/arffFile/$ARFF"
    rm -f $FILEARFF
    echo -e "@relation $(echo $ARFF | cut -d'.' -f1)\n" >> $FILEARFF
    echo -e "@attribute nodeId integer" >> $FILEARFF
    #echo -e "@attribute dimensions relational" >> $FILEARFF
    DIMENSIONS=$(cat $embFile | head -n1 | cut -d' ' -f2)
    for ((i=1;i<=$DIMENSIONS;i++)); do
        echo -e "@attribute dim$i real" >> $FILEARFF
    done
    #echo -e "@end dimensions real" >> $FILEARFF
    echo -e "\n@data" >> $FILEARFF
    FIRSTLINE=1
    while read $l; do
        if [ "$FIRSTLINE" == "1" ]; then
            FIRSTLINE=0
            continue
        fi
        sed -e 's/ /,/g' $l >> $FILEARFF
    done < $embFile
    echo $ARFF
    exit
done
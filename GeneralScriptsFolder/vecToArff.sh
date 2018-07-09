#!/bin/bash
SCRIPTPATH=$PWD

mkdir -p $SCRIPTPATH/arffFile

for vecFile in $( find $SCRIPTPATH -name "*.txt") ; do
    if [ -z "$(echo $vecFile | grep "vector_")" ]; then
        continue
    fi
    ARFF="$( echo ${vecFile##*/} | cut -d'.' -f1 ).arff"
    FILEARFF="$SCRIPTPATH/arffFile/$ARFF"
    rm -f $FILEARFF
    echo -e "@relation $(echo $ARFF | cut -d'.' -f1)\n" >> $FILEARFF
    DIMENSIONS=$((($(echo $ARFF | cut -d'_' -f2)*$(echo $ARFF | cut -d'_' -f3 | cut -d'.' -f1))))
    for ((i=1;i<=$DIMENSIONS;i++)); do
        echo -e "@attribute dim$i real" >> $FILEARFF
    done
    echo -e "@attribute buggy {1, 0}" >> $FILEARFF
    echo -e "\n@data" >> $FILEARFF
    cat $vecFile >> $FILEARFF
done
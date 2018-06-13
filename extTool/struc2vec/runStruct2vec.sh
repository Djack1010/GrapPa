#!/bin/bash
SCRIPTPATH=$PWD
STRUCT2VECPATH="/home/djack/Dropbox/thesis/external_material/struc2vec"

if [ ! -f $STRUCT2VECPATH/src/main.py ]; then
    echo "ERROR, set struct2vec path! Exiting..."
    exit
fi

if [ ! -d $SCRIPTPATH/graph ] || [ ! -d $SCRIPTPATH/emb ]; then
    echo "ERROR, graph/emb folder not found! Exiting..."
    exit
fi

for edgeList in $SCRIPTPATH/graph/*.edgelist; do
    NAMEFILE=$( echo ${edgeList##*/} | cut -d'.' -f1 )
    echo "STARTING $NAMEFILE"
    time python $STRUCT2VECPATH/src/main.py --input $edgeList --output $SCRIPTPATH/emb/$NAMEFILE.emb --directed --OPT2 true
    echo "DONE!"
    exit
done
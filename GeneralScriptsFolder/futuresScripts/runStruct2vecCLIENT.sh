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

if [ -z $1 ]; then
    echo "NAME not set, exiting..."
    exit
else
    EDGENAME=$1
fi

NAMEFILE=$( echo ${1##*/} | cut -d'.' -f1 )
echo "STARTING $NAMEFILE at $(date)" >> $SCRIPTPATH/clientLog.txt
python $STRUCT2VECPATH/src/main.py --input $EDGENAME --output $SCRIPTPATH/emb/$NAMEFILE.emb --directed --dimensions 50 --OPT1 true --OPT2 true 2>> $SCRIPTPATH/clientErrors/errors$NAMEFILE
echo "FINISHING $NAMEFILE at $(date)" >> $SCRIPTPATH/clientLog.txt
exit
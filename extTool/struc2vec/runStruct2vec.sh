#!/bin/bash
SCRIPTPATH=$PWD
STRUCT2VECPATH="/home/djack/Dropbox/thesis/external_material/struc2vec"

function progrBar {
    #[##################################################] (100%)
    PAR=$1
    TOT=$2
    PER=$(bc <<< "scale = 2; ($PAR / $TOT) * 100")
    TEMPPER=$( echo $PER | cut -d'.' -f1)
    COUNT=0
    #echo -ne ""\\r
    echo -e "\033[2A"
    #echo -e "\n"
    echo "$PAR out of $TOT"
    echo -ne "["
    while [ "$TEMPPER" -gt "0" ]; do
        TEMPPER=$(($TEMPPER-2))
        echo -ne "#"
        COUNT=$(($COUNT+1))
    done
    COUNT=$((50-$COUNT))
    for (( c=1; c<$COUNT; c++ )); do
        echo -ne "-"
    done  
    echo -ne "] ($PER%)"
    if ! [ -z "$PIDRUN" ]; then
        TIMERUN=$( ps -o etime= -p "$PIDRUN" )
        echo -ne " TIME:$TIMERUN"
    fi
}

if [ ! -f $STRUCT2VECPATH/src/main.py ]; then
    echo "ERROR, set struct2vec path! Exiting..."
    exit
fi

if [ ! -d $SCRIPTPATH/graph ] || [ ! -d $SCRIPTPATH/emb ]; then
    echo "ERROR, graph/emb folder not found! Exiting..."
    exit
fi

echo -e "\n"
rm -f $SCRIPTPATH/log.txt
PIDRUN=$$
TOTFILE=$(find $SCRIPTPATH/graph/base | wc -l)
COUNTPAR=0
for edgeList in $SCRIPTPATH/graph/base/*.edgelist; do
    NAMEFILE=$( echo ${edgeList##*/} | cut -d'.' -f1 )
    echo "STARTING $NAMEFILE at $(date)" >> $SCRIPTPATH/log.txt
    if [ -f $SCRIPTPATH/emb/$NAMEFILE.emb ]; then
        echo "$NAMEFILE.emb aleady exist, skipping vectorization..."
    else
        python $STRUCT2VECPATH/src/main.py --input $edgeList --output $SCRIPTPATH/emb/$NAMEFILE.emb --directed --dimensions 50 --OPT1 true --OPT2 true >> $SCRIPTPATH/log.txt 2>&1 &
        while [ "$(jobs | grep "Running")" ]; do
            progrBar $COUNTPAR $TOTFILE
            sleep 15
        done
    fi
    echo "DONE $NAMEFILE at $(date)" >> $SCRIPTPATH/log.txt
    COUNTPAR=$(($COUNTPAR+1))
done
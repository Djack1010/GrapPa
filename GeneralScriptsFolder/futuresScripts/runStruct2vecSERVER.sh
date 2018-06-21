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
    echo "$PAR out of $TOT - RUNNING $(jobs | grep "Running" | wc -l)"
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

if [ -z $1 ]; then
    echo "MAX number parallel process not set, exiting..."
    exit
else
    MAX=$1
fi

rm -f $SCRIPTPATH/clientLog.txt
rm -rf $SCRIPTPATH/clientErrors
mkdir clientErrors
JOBSARRAY=()
TOTFILE=0
PIDRUN=$$
UPDATE=30
echo -e "\n"
for edgeList in $SCRIPTPATH/graph/base/*.edgelist; do
    JOBSARRAY+=("$edgeList")
    TOTFILE=$(($TOTFILE+1))
done
IND=0
while true; do
    if [ "$IND" -ge "$TOTFILE" ]; then
        while [ "$(jobs | grep "Running" )" ]; do
            PARNOW=$(($IND-$(jobs | wc -l)))
            progrBar $PARNOW $TOTFILE
            sleep $UPDATE
        done
        break
    elif [ "$(jobs | grep "Running" | wc -l)" -lt "$MAX" ]; then
        #echo "$IND out of $TOTFILE - JOBS RUNNING: $(jobs | wc -l)"
        $SCRIPTPATH/runStruct2vecCLIENT.sh ${JOBSARRAY[$IND]} &
        IND=$(($IND+1))
    else
        #echo "BUSY SITUATION - JOBS RUNNING: $(jobs | wc -l)"
        PARNOW=$(($IND-$(jobs | wc -l)))
        progrBar $PARNOW $TOTFILE
        sleep $UPDATE
    fi
done
echo -e "\nDONE!"
exit
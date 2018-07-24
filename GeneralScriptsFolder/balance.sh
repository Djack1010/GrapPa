#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

if [ -f "$SCRIPTPATH/base_COM" ] || [ -f "$SCRIPTPATH/base_SeT" ]; then
    echo "ERROR, base_COM or base_SeT not found"
    exit
fi

function progrBar {
    #[##################################################] (100%)
    echo -e "\033[3A"
    PAR=$1
    TOT=$2
    PER=$(bc <<< "scale = 2; ($PAR / $TOT) * 100" | cut -d'.' -f1)
    TEMPPER=$( echo $PER | cut -d'.' -f1)
    COUNT=0
    echo "PROGRESS: $PAR out of $TOT"
    echo -ne "["
    while [ "$TEMPPER" -gt "0" ]; do
        TEMPPER=$(($TEMPPER-2))
        echo -ne "#"
        COUNT=$(($COUNT+1))
    done
    COUNT=$((50-$COUNT))
    for (( c=0; c<$COUNT; c++ )); do
        echo -ne "-"
    done  
    echo -ne "] ($PER%)"
    if [ ! -z "$PIDRUN" ]; then
        TIMERUN=$( ps -o etime= -p "$PIDRUN" )
        echo -ne " TIME:$TIMERUN"
    fi
    echo ""
}

MUT=$(($(ls $SCRIPTPATH/base_SeT | wc -l)-$(ls $SCRIPTPATH/base_SeT | grep "_0" | wc -l)))
ORI=$(($(ls $SCRIPTPATH/base_SeT | wc -l)-$MUT))
echo "TOT: $(ls $SCRIPTPATH/base_SeT | wc -l) MUT: $MUT ORI: $ORI"
DIFF=$(($MUT-$ORI))
IND=0
echo -e "DIFF: $DIFF\n\n"
while [ "$IND" != "$DIFF" ]; do
    PASS=0
    while [ "$PASS" == "0" ]; do
        FILETOMOVE=$(ls $SCRIPTPATH/generic/base_SeT | shuf -n 1)
        if [ "$(find $SCRIPTPATH/base_SeT -name "$FILETOMOVE")" ]; then
            continue
        else
            PASS=1
        fi
    done
    cp $SCRIPTPATH/generic/base_SeT/$FILETOMOVE $SCRIPTPATH/base_SeT
    progrBar $IND $DIFF
    IND=$(($IND+1))
done
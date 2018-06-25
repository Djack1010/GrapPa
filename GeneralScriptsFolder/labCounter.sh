#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
MAX=9000

if [ -z $1 ]; then
    FIRSTOCC=100
else
    FIRSTOCC=$1
fi
if [ -z $2 ]; then
    STARTLAB=115
else
    STARTLAB=$2
fi
if [ -z $3 ]; then
    rm -f $SCRIPTPATH/Counter
    touch $SCRIPTPATH/Counter
else
    SKIP=SET
fi

#PIDRUN=$$
# echo -e "\033[2A"
function progrBar {
    #[##################################################] (100%)
    PAR=$1
    TOT=$2
    PER=$(bc <<< "scale = 2; ($PAR / $TOT) * 100")
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
    for (( c=1; c<$COUNT; c++ )); do
        echo -ne "-"
    done  
    echo -ne "] ($PER%)"
    if ! [ -z "$PIDRUN" ]; then
        TIMERUN=$( ps -o etime= -p "$PIDRUN" )
        echo -ne " TIME:$TIMERUN"
    fi
    echo -e "\033[2A"
}

FTOT=$(ls | wc -l)
FNOW=0
PIDRUN=$$
if [ -z $SKIP ]; then
    for f in $SCRIPTPATH/*.txt; do
        while read l; do
            if [ "$(echo "$l" | grep "lab_")" ]; then
                REMOVENAME=$(echo "$l" | cut -d' ' -f1)
                TEMPLINE=$(echo "$l" | sed -e "s/$REMOVENAME //g")
                TEMPLINE4GREP=$(echo $TEMPLINE | sed -e 's/\./\\\./g; s/\*/\\*/g; s/\[/\\[/g; s/\]/\\]/g')
                #echo "$TEMPLINE"
                LABSTORED=$( cat $SCRIPTPATH/Counter | grep "$TEMPLINE4GREP"_ )
                if [ "$LABSTORED" ] ; then
                    #echo "1. $TEMPLINE - $TEMPLINE4GREP - $LABSTORED"
                    NUM=$( echo $LABSTORED | cut -d'_' -f3 )
                    NUM=$(($NUM+1))
                    sed -i "/$TEMPLINE4GREP/d" $SCRIPTPATH/Counter
                    echo $TEMPLINE"_"$NUM >> $SCRIPTPATH/Counter
                    #echo "UPDATED!"
                else
                    echo -e $TEMPLINE"_1" >> $SCRIPTPATH/Counter
                    #echo "NEW!"
                fi
            else
                continue
            fi
        done <$f
        FNOW=$(($FNOW+1))
        progrBar $FNOW $FTOT
    done
    sort -nr -t_ -k3 $SCRIPTPATH/Counter -o $SCRIPTPATH/Counter
fi

IND=0
LINE=0
TOTLINE=$(cat $SCRIPTPATH/Counter | wc -l)
LABNUM=$STARTLAB
while read l; do
    #echo "$(echo $l | cut -d'_' -f2)_$LAB" >> $SCRIPTPATH/mapNodeLabel
    LAB=$(echo $l | cut -d'_' -f2 | sed -e 's/\./\\\./g; s/\*/\\*/g; s/\[/\\[/g; s/\]/\\]/g')
    sed -i "s/lab_$LAB/$LABNUM/g" $SCRIPTPATH/*.txt
    progrBar $LINE $TOTLINE
    LINE=$(($LINE+1))
    if [ "$IND" == "$FIRSTOCC" ]; then
        continue
    else
        IND=$(($IND+1))
        LABNUM=$(($LABNUM+1))
    fi
done < $SCRIPTPATH/Counter
#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

echo "ARGUMENTS REQUIRED (enter for default [XXX]):"
echo "1) Top Number of occurences to select [100]"
echo "2) Starting label number [115]"
echo "3) Skip generation Counter.txt file [YES]"
echo -e "\t3a) format file\n [adjlist]"
echo "ARGUMENT 1 - Top Number of occurences to select"
read FIRSTOCC
if [ -z "$FIRSTOCC" ]; then
    FIRSTOCC=100
fi
echo "SET: $FIRSTOCC"
echo "ARGUMENT 2 - Starting label number"
read STARTLAB
if [ -z "$STARTLAB" ]; then
    STARTLAB=115
fi
echo "SET: $STARTLAB"
echo "ARGUMENT 3 - Skip generation Counter.txt file? [Y/n]"
read -n1 ARG3
if [ "$ARG3" != "n" ]; then
    SKIP="SET"
else
    rm -f $SCRIPTPATH/Counter
    touch $SCRIPTPATH/Counter
    echo -e "\nARGUMENT 3a - Format file (txt, adjlist, etc...)"
    read ARG3a
    if [ -z "$ARG3a" ]; then
        ARG3a="adjlist"
    fi
    echo "SET: $ARG3a"
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

FTOT=$(find $SCRIPTPATH -name "*.$ARG3a" | wc -l)
FNOW=0
PIDRUN=$$
if [ -z $SKIP ]; then
    for f in $(find $SCRIPTPATH -name "*.$ARG3a") ; do
        if [ ! -f "$f" ]; then
            continue
        fi
        while read l; do
            if [ "$(echo "$l" | grep "IDE_")" ]; then
                TLABEL=$(echo "$l" | cut -d' ' -f2 )
                #REMOVENAME=$(echo "$l" | cut -d' ' -f1)
                #echo $REMOVENAME
                #TEMPLINE=$(echo "$l" | sed -e "s/$REMOVENAME //g")
                #echo $TEMPLINE
                TEMPLINE4GREP=$(echo $TLABEL | sed -e 's/\./\\\./g; s/\*/\\*/g; s/\[/\\[/g; s/\]/\\]/g')
                #echo $TEMPLINE4GREP
                #echo "$TEMPLINE"
                LABSTORED=$( cat $SCRIPTPATH/Counter | grep "$TEMPLINE4GREP"_ )
                if [ "$LABSTORED" ] ; then
                    #echo "1. $TEMPLINE - $TEMPLINE4GREP - $LABSTORED"
                    NUM=$( echo $LABSTORED | cut -d'_' -f3 )
                    NUM=$(($NUM+1))
                    sed -i "/$TEMPLINE4GREP/d" $SCRIPTPATH/Counter
                    echo $TLABEL"_"$NUM >> $SCRIPTPATH/Counter
                    #echo "UPDATED!"
                else
                    echo -e $TLABEL"_1" >> $SCRIPTPATH/Counter
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
exit
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
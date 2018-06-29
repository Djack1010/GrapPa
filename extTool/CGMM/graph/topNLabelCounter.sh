#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

echo "ARGUMENTS REQUIRED (enter for default [XXX]):"
echo "0) Input Folder []"
echo "1a) Top Number of occurences to select for IDE [100]"
echo "1b) Top Number of occurences to select for LIT [30]"
echo "2) Starting label number [115]"
echo "3) format file\n [adjlist]"
echo "4) Select Mode [all]"
echo -e "\t'a' for all (Counter and Replace)"
echo -e "\t'c' for only Counter"
echo -e "\t'r' for only Replace (usind Counter_INPUTFOLDER file)"
echo "ARGUMENT 0 - Input folder (from script path, NO ending and starting slash)"
read INPUTFOLDER
if [ -z "$INPUTFOLDER" ] || [ ! -d $SCRIPTPATH/$INPUTFOLDER ]; then
    echo "ERROR! Insert valid Input Folder, exiting..."
    exit
fi
echo "SET: $SCRIPTPATH/$INPUTFOLDER"
echo "ARGUMENT 1a - Top Number of occurences to select for IDE"
read FIRSTOCC_IDE
if [ -z "$FIRSTOCC_IDE" ]; then
    FIRSTOCC_IDE=100
fi
echo "SET: $FIRSTOCC_IDE"
echo "ARGUMENT 1b - Top Number of occurences to select for LIT"
read FIRSTOCC_LIT
if [ -z "$FIRSTOCC_LIT" ]; then
    FIRSTOCC_LIT=30
fi
echo "SET: $FIRSTOCC_LIT"
echo "ARGUMENT 2 - Starting label number"
read STARTLAB
if [ -z "$STARTLAB" ]; then
    STARTLAB=115
fi
echo "SET: $STARTLAB"
echo -e "\nARGUMENT 3 - Format file (txt, adjlist, etc...)"
read ARG4
if [ -z "$ARG4" ]; then
    ARG4="adjlist"
fi
if [ -z "$(find $SCRIPTPATH/$INPUTFOLDER -name "*.$ARG4")" ]; then
    echo "No file found with '.$ARG4' format, exiting..."
    exit
fi
echo "SET: $ARG4"
echo "ARGUMENT 4 - Select mode 'a', 'c' or 'r' [a]"
read -n1 ARG3
if [ "$ARG3" == "c" ]; then
    echo "SET: counter mode"
    CPH="SET"
    rm -f $SCRIPTPATH/CounterIDE
    touch $SCRIPTPATH/CounterIDE
    rm -f $SCRIPTPATH/CounterLIT
    touch $SCRIPTPATH/CounterLI
elif [ "$ARG3" == "r" ]; then
    echo "SET: replace mode"
    RPH="SET"
    if [ ! -f $SCRIPTPATH/CounterIDE ] || [ ! -f $SCRIPTPATH/CounterLIT ]; then
        echo "ERROR! CounterIDE or CounterLIT not found, exiting..."
        exit
    fi
else
    echo "SET: all mode"
    CPH="SET"
    rm -f $SCRIPTPATH/CounterIDE
    touch $SCRIPTPATH/CounterIDE
    rm -f $SCRIPTPATH/CounterLIT
    touch $SCRIPTPATH/CounterLIT
    RPH="SET"
fi

#PIDRUN=$$
# echo -e "\033[2A"
function progrBar {
    #[##################################################] (100%)
    echo -e "\033[3A"
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
    if [ ! -z "$PIDRUN" ]; then
        TIMERUN=$( ps -o etime= -p "$PIDRUN" )
        echo -ne " TIME:$TIMERUN"
    fi
    echo ""
}

echo -e "\n" #for progrBar

FTOT=$(find $SCRIPTPATH/$INPUTFOLDER -name "*.$ARG4" | wc -l)
FNOW=0
PIDRUN=$$
if [ "$CPH" ]; then
    for f in $(find $SCRIPTPATH/$INPUTFOLDER -name "*.$ARG4") ; do
        if [ ! -f "$f" ]; then
            continue
        fi
        while read l; do
            if [ "$(echo "$l" | grep " IDE_" )" ]; then
                TLABEL=$(echo "$l" | cut -d' ' -f2 )
                #REMOVENAME=$(echo "$l" | cut -d' ' -f1)
                #echo $REMOVENAME
                #TEMPLINE=$(echo "$l" | sed -e "s/$REMOVENAME //g")
                #echo $TEMPLINE
                TEMPLINE4GREP=$(echo $TLABEL | sed -e 's/\./\\\./g; s/\$/\\\$/g; s/\*/\\*/g; s/\[/\\[/g; s/\]/\\]/g')
                #echo $TEMPLINE4GREP
                #echo "$TEMPLINE"
                LABSTORED=$( cat $SCRIPTPATH/CounterIDE | grep ${TEMPLINE4GREP}_ )
                #echo "1. $TLABEL - $TEMPLINE4GREP - $LABSTORED"
                if [ "$LABSTORED" ] ; then
                    NUM=$( echo $LABSTORED | cut -d'_' -f3 )
                    NUM=$(($NUM+1))
                    sed -i "/${TEMPLINE4GREP}_/d" $SCRIPTPATH/CounterIDE
                    echo $TLABEL"_"$NUM >> $SCRIPTPATH/CounterIDE
                    #echo "UPDATED!"
                else
                    echo -e $TLABEL"_1" >> $SCRIPTPATH/CounterIDE
                    #echo "NEW!"
                fi
            elif [ "$(echo "$l" | grep " LIT_")" ]; then
                TLABEL=$(echo "$l" | cut -d' ' -f2 )
                TEMPLINE4GREP=$(echo $TLABEL | sed -e 's/\./\\\./g; s/\$/\\\$/g; s/\*/\\*/g; s/\[/\\[/g; s/\]/\\]/g')
                LABSTORED=$( cat $SCRIPTPATH/CounterLIT | grep ${TEMPLINE4GREP}_ )
                if [ "$LABSTORED" ] ; then
                    #echo "1. $TEMPLINE - $TEMPLINE4GREP - $LABSTORED"
                    NUM=$( echo $LABSTORED | cut -d'_' -f3 )
                    NUM=$(($NUM+1))
                    sed -i "/${TEMPLINE4GREP}_/d" $SCRIPTPATH/CounterLIT
                    echo $TLABEL"_"$NUM >> $SCRIPTPATH/CounterLIT
                    #echo "UPDATED!"
                else
                    echo -e $TLABEL"_1" >> $SCRIPTPATH/CounterLIT
                    #echo "NEW!"
                fi
            else
                continue
            fi
        done <$f
        FNOW=$(($FNOW+1))
        progrBar $FNOW $FTOT
    done
    sort -nr -t_ -k3 "${SCRIPTPATH}/CounterIDE" -o "${SCRIPTPATH}/CounterIDE"
    sort -nr -t_ -k3 "${SCRIPTPATH}/CounterLIT" -o "${SCRIPTPATH}/CounterLIT"
fi

if [ "$RPH" ]; then
    IND=0
    LINE=0
    TOTLINE=$(($(cat $SCRIPTPATH/CounterIDE | wc -l)+$(cat $SCRIPTPATH/CounterLIT | wc -l)))
    LABNUM=$STARTLAB
    while read l; do
        #echo "$(echo $l | cut -d'_' -f2)_$LAB" >> $SCRIPTPATH/mapNodeLabel
        LAB=$(echo $l | cut -d'_' -f2 | sed -e 's/\./\\\./g; s/\*/\\*/g; s/\[/\\[/g; s/\]/\\]/g')
        sed -i "s/IDE_$LAB /$LABNUM /g" $SCRIPTPATH/$INPUTFOLDER/*.$ARG4
        progrBar $LINE $TOTLINE
        LINE=$(($LINE+1))
        if [ "$IND" == "$FIRSTOCC_IDE" ]; then
            continue
        else
            IND=$(($IND+1))
            LABNUM=$(($LABNUM+1))
        fi
    done < $SCRIPTPATH/CounterIDE
    IND=0
    while read l; do
        #echo "$(echo $l | cut -d'_' -f2)_$LAB" >> $SCRIPTPATH/mapNodeLabel
        LAB=$(echo $l | cut -d'_' -f2 | sed -e 's/\./\\\./g; s/\*/\\*/g; s/\[/\\[/g; s/\]/\\]/g')
        sed -i "s/LIT_$LAB /$LABNUM /g" $SCRIPTPATH/$INPUTFOLDER/*.$ARG4
        progrBar $LINE $TOTLINE
        LINE=$(($LINE+1))
        if [ "$IND" == "$FIRSTOCC_LIT" ]; then
            continue
        else
            IND=$(($IND+1))
            LABNUM=$(($LABNUM+1))
        fi
    done < $SCRIPTPATH/CounterLIT

    echo -e "GREATEST LABEL USED: $LABNUM"
fi

echo "FINAL CHECK..."
for nf in $(find $SCRIPTPATH/$INPUTFOLDER -name "*.$ARG4") ; do
    if [ "$(cat $nf | grep "IDE" )" ] || [ "$(cat $nf | grep "LIT" )" ]; then 
        echo "ERROR, something still in $nf"
        ERR="SET"
    fi
done
if [ "$ERR" ]; then
    echo "FINISHED with error..."
else
    echo "FINISHED SUCCESFULLY!"
fi

exit
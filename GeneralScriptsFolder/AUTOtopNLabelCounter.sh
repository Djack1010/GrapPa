#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
PIDRUN=$$

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "ERROR, set arguments, exiting..."
    exit
else
    INPUTPATH=$1
    INPUTFOLDER=$2
fi
if [ ! -d $INPUTPATH/$INPUTFOLDER ]; then
    echo "ERROR! Insert valid Input Folder, exiting..."
    exit
fi

if [ -z "$STARTLAB" ]; then
    STARTLAB=114
fi

if [ -z "$ARG4" ]; then
    ARG4="adjlist"
fi
if [ -z "$(find $INPUTPATH/$INPUTFOLDER -name "*.$ARG4")" ]; then
    echo "No file found with '.$ARG4' format, exiting..."
    exit
fi


#CPH="SET"
rm -f $SCRIPTPATH/CounterLIT
rm -f $SCRIPTPATH/CounterIDE
if [ ! -d $SCRIPTPATH/../extTool/CGMM/info_model40x8 ]; then
    echo "info_model40x8 not found, exiting..."
    exit
fi
cp $SCRIPTPATH/../extTool/CGMM/info_model40x8/CounterIDE $SCRIPTPATH/CounterIDE
cp $SCRIPTPATH/../extTool/CGMM/info_model40x8/CounterLIT $SCRIPTPATH/CounterLIT
#touch $SCRIPTPATH/CounterIDE
#touch $SCRIPTPATH/CounterLIT
if [ -d $SCRIPTPATH/info ];then 
    rm -r $SCRIPTPATH/info
fi
RPH="SET"

mkdir -p ~/.mylock
rm -f ~/.mylock/lockCounterIDE${PIDRUN}
touch ~/.mylock/lockCounterIDE${PIDRUN}
rm -f ~/.mylock/lockCounterLIT${PIDRUN}
touch ~/.mylock/lockCounterLIT${PIDRUN}

#PIDRUN=$$
# echo -e "\033[2A"
function progrBar {
    #[##################################################] (100%)
    echo -e "\033[3A"
    PAR=$(($1-$(jobs | grep "Running" | wc -l)))
    TOT=$2
    PER=$(bc <<< "scale = 2; ($PAR / $TOT) * 100")
    TEMPPER=$( echo $PER | cut -d'.' -f1)
    COUNT=0
    echo "PROGRESS: $PAR out of $TOT - RUNNING: $(jobs | grep "Running" | wc -l)"
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

function SUBsuggTopN {
    if [ -z "$1" ]; then
        SPER=90
    else
        SPER=$1
    fi
    SUBNUM=0
    for l in $(cat $SCRIPTPATH/CounterLIT | cut -d'_' -f3 ); do
        SUBNUM=$(($SUBNUM+$l))
    done
    SUBNUM=$(bc <<< "scale = 2; ($SUBNUM / 100) * $SPER ")
    SUBNUM=$(echo $SUBNUM | cut -d'.' -f1)
    TOPA=0
    while read l; do
        SUBNUM=$(($SUBNUM-$(echo $l | cut -d'_' -f3 )))
        if [ "$SUBNUM" -le "0" ]; then
            break
        else
            TOPA=$(($TOPA+1))
        fi
    done < $SCRIPTPATH/CounterLIT
    FIRSTOCC_LIT=$TOPA
    SUBNUM=0
    for l in $(cat $SCRIPTPATH/CounterIDE | cut -d'_' -f3 ); do
        SUBNUM=$(($SUBNUM+$l))
    done
    SUBNUM=$(bc <<< "scale = 2; ($SUBNUM / 100) * $SPER ")
    SUBNUM=$(echo $SUBNUM | cut -d'.' -f1)
    TOPB=0
    while read l; do
        SUBNUM=$(($SUBNUM-$(echo $l | cut -d'_' -f3 )))
        if [ "$SUBNUM" -le "0" ]; then
            break
        else
            TOPB=$(($TOPB+1))
        fi
    done < $SCRIPTPATH/CounterIDE
    FIRSTOCC_IDE=$TOPB
}

function counterSubProcess {
    while read l; do
        if [ "$(echo "$l" | grep " IDE_" )" ]; then
            TLABEL=$(echo "$l" | cut -d' ' -f2 )
            #REMOVENAME=$(echo "$l" | cut -d' ' -f1)
            #echo $REMOVENAME
            #TEMPLINE=$(echo "$l" | sed -e "s/$REMOVENAME //g")
            #echo $TEMPLINE
            TEMPLINE4GREP=$(sed 's/[^^]/[&]/g; s/\^/\\^/g' <<<"$TLABEL")
            #echo $TEMPLINE4GREP
            #echo "$TEMPLINE"
            (
                flock 200
                LABSTORED=$( cat $2/CounterIDE | grep -F ${TLABEL}_ )
                #echo "1. $TLABEL - $TEMPLINE4GREP - $LABSTORED"
                if [ "$LABSTORED" ] ; then
                    if [ "$(echo $LABSTORED | wc -w)" -gt "1" ]; then
                        echo "ERRORE, too many lines found in CounterLIT..." >&2
                        echo "1. $TLABEL - $TEMPLINE4GREP - $LABSTORED" >&2
                    fi
                    NUM=$( echo $LABSTORED | cut -d'_' -f3 )
                    NUM=$(($NUM+1))
                    sed -i "/${TEMPLINE4GREP}_/d" $2/CounterIDE
                    echo $TLABEL"_"$NUM >> $2/CounterIDE
                    #echo "UPDATED!"
                else
                    echo -e $TLABEL"_1" >> $2/CounterIDE
                    #echo "NEW!"
                fi
            ) 200>~/.mylock/lockCounterIDE$3
        elif [ "$(echo "$l" | grep " LIT_")" ]; then
            TLABEL=$(echo "$l" | cut -d' ' -f2 )
            TEMPLINE4GREP=$(sed 's/[^^]/[&]/g; s/\^/\\^/g' <<<"$TLABEL")
            (
                flock 200
                LABSTORED=$( cat $2/CounterLIT | grep -F ${TLABEL}_ )
                if [ "$LABSTORED" ] ; then
                    if [ "$(echo $LABSTORED | wc -w)" -gt "1" ]; then
                        echo "ERRORE, too many lines found in CounterLIT..." >&2
                        echo "1. $TLABEL - $TEMPLINE4GREP - $LABSTORED" >&2
                    fi
                    NUM=$( echo $LABSTORED | cut -d'_' -f3 )
                    NUM=$(($NUM+1))
                    sed -i "/${TEMPLINE4GREP}_/d" $2/CounterLIT
                    echo $TLABEL"_"$NUM >> $2/CounterLIT
                    #echo "UPDATED!"
                else
                    echo -e $TLABEL"_1" >> $2/CounterLIT
                    #echo "NEW!"
                fi
            ) 200>~/.mylock/lockCounterLIT$3
        else
            continue
        fi
    done <$1
}

FTOT=$(find $INPUTPATH/$INPUTFOLDER -name "*.$ARG4" | wc -l)
FNOW=0
#------------SET VARIABLE-------------
MAXJOBS=8
UPDATE=0.5
#-------------------------------------
if [ "$CPH" ]; then
    echo "START COUNTING"
    echo -e "\n" #for progrBar
    for f in $(find $INPUTPATH/$INPUTFOLDER -name "*.$ARG4") ; do
        if [ ! -f "$f" ]; then
            continue
        fi
        while true; do
            if [ "$(jobs | grep "Running" | wc -l)" -lt "$MAXJOBS" ]; then
                #echo "$IND out of $KNUM - JOBS RUNNING: $(jobs | wc -l)"
                counterSubProcess $f $SCRIPTPATH ${PIDRUN} &
                break
            else
                #echo "BUSY SITUATION - JOBS RUNNING: $(jobs | wc -l)"
                progrBar $FNOW $FTOT
                sleep $UPDATE
            fi
        done
        FNOW=$(($FNOW+1))
        progrBar $FNOW $FTOT
    done
    while [ "$(jobs | grep "Running" )" ]; do
        progrBar $FNOW $FTOT
        sleep $UPDATE
    done
    sort -nr -t_ -k3 "${SCRIPTPATH}/CounterIDE" -o "${SCRIPTPATH}/CounterIDE"
    sort -nr -t_ -k3 "${SCRIPTPATH}/CounterLIT" -o "${SCRIPTPATH}/CounterLIT"
    echo "CHECKING COUNTER OPERATION..."
    while read l; do
        LAB=$(echo ${l} | cut -d'_' -f2)
        NUM=$(cat $SCRIPTPATH/CounterLIT | grep -F _${LAB}_ | wc -l)
        if [ "$NUM" -gt "1" ]; then
            echo "More than one for $LAB" >&2
            ERRC="SET"
        fi
    done < $SCRIPTPATH/CounterLIT
    while read l; do
        LAB=$(echo ${l} | cut -d'_' -f2)
        NUM=$(cat $SCRIPTPATH/CounterIDE | grep -F _${LAB}_ | wc -l)
        if [ "$NUM" -gt "1" ]; then
            echo "More than one for $LAB" >&2
            ERRC="SET"
        fi
    done < $SCRIPTPATH/CounterIDE
    if [ "$ERRC" ];then
        echo "FINISHED with error..."
    else
        echo "FINISHED SUCCESFULLY!"
        SUBsuggTopN
    fi
fi

echo -e ""

if [ "$RPH" ]; then
    echo "START REPLACING"
    SUBsuggTopN
    echo -e "\n" #for progrBar
    IND=0
    LINE=0
    TOTLINE=$(($(cat $SCRIPTPATH/CounterIDE | wc -l)+$(cat $SCRIPTPATH/CounterLIT | wc -l)))
    LABNUM=$STARTLAB
    while read l; do
        #echo "$(echo $l | cut -d'_' -f2)_$LAB" >> $SCRIPTPATH/mapNodeLabel
        LAB=$(echo $l | cut -d'_' -f2)
        TEMPLINE4GREP=$(sed 's/[^^]/[&]/g; s/\^/\\^/g' <<<"$LAB")
        sed -i "s/IDE_$TEMPLINE4GREP /$LABNUM /g" $INPUTPATH/$INPUTFOLDER/*.$ARG4
        progrBar $LINE $TOTLINE
        LINE=$(($LINE+1))
        if [ "$IND" == "$FIRSTOCC_IDE" ]; then
            continue
        else
            IND=$(($IND+1))
            LABNUM=$(($LABNUM+1))
        fi
    done < $SCRIPTPATH/CounterIDE
    sed -i "s/ IDE_.* (/ $LABNUM (/g" $INPUTPATH/$INPUTFOLDER/*.$ARG4

    IND=0
    while read l; do
        #echo "$(echo $l | cut -d'_' -f2)_$LAB" >> $SCRIPTPATH/mapNodeLabel
        LAB=$(echo $l | cut -d'_' -f2)
        TEMPLINE4GREP=$(sed 's/[^^]/[&]/g; s/\^/\\^/g' <<<"$LAB")
        sed -i "s/LIT_$TEMPLINE4GREP /$LABNUM /g" $INPUTPATH/$INPUTFOLDER/*.$ARG4
        progrBar $LINE $TOTLINE
        LINE=$(($LINE+1))
        if [ "$IND" == "$FIRSTOCC_LIT" ]; then
            continue
        else
            IND=$(($IND+1))
            LABNUM=$(($LABNUM+1))
        fi
    done < $SCRIPTPATH/CounterLIT
    sed -i "s/ LIT_.* (/ $LABNUM (/g" $INPUTPATH/$INPUTFOLDER/*.$ARG4

    echo -e "GREATEST LABEL USED: $LABNUM"

    echo "FINAL CHECK..."
    for nf in $(find $INPUTPATH/$INPUTFOLDER -name "*.$ARG4") ; do
        if [ "$(cat $nf | grep "IDE" )" ] || [ "$(cat $nf | grep "LIT" )" ]; then 
            echo "ERROR, something still in $nf" >&2
            ERRF="SET" >&2
        fi
    done
    if [ "$ERRF" ]; then
        echo "FINISHED with error..."
    else
        echo "FINISHED SUCCESFULLY!"
    fi
fi

rm -f $SCRIPTPATH/CounterIDE
rm -f $SCRIPTPATH/CounterLIT

LABNUM=$(($LABNUM+1))
echo $LABNUM >> $SCRIPTPATH/temp_passData

exit
#!/bin/bash

if [ -z $1 ]; then
    SLEEPTIME=20
else
    SLEEPTIME=$1
fi

function progrBar {
    #[##################################################] (100%)
    PAR=$1
    TOT=$2
    PER=$(bc <<< "scale = 2; ($PAR / $TOT) * 100")
    TEMPPER=$( echo $PER | cut -d'.' -f1)
    COUNT=0
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

MUTATION_FOLDER=$(cat config.txt | grep "MUTATION_FOLDER" | cut -d"=" -f2)
if [ ! -d "$MUTATION_FOLDER" ]; then
    echo "ERROR: Set the MUTATION_FOLDER variable in config.txt! Exiting..."
    exit
fi

#|| [ "$(ps aux | grep "./run.sh -targ" | wc -l )" -gt "1" ]
if [ "$(ps aux | grep "./run.sh -allclasses" | wc -l )" -gt "1" ]; then
    MUTTOT=$( ls $MUTATION_FOLDER | wc -l )
    PIDRUN=$(pgrep "run.sh")
    while [ "$(ps aux | grep "./run.sh -allclasses" | wc -l )" -gt "1" ]; do
        MUTLOSE=$(cat result.txt | grep "MUT (true)" | grep "Success perc. (0)" | wc -l)
        MUTNOFO=$(cat result.txt | grep "MUT (true)" | grep "Total Success perc. (N.A.) METHOD-NOT-FOUND!" | wc -l)
        #MUTLOSE=$(($MUTLOSE/2))
        MUTGET=$(cat result.txt | grep "MUT (true)" | grep "Success perc. (100)" | wc -l)
        MUTERR=$(cat errors.txt | grep "ERROR -> " | wc -l)
        MUTPAR=$(($MUTGET+$MUTLOSE+$MUTNOFO+$MUTERR))
        echo -ne "$(cat log.txt | grep PROGRESS | tail -n1)\n"
        echo -ne "FAIL: $MUTLOSE - NOTFOUND: $MUTNOFO ERROR: $MUTERR SUCC: $MUTGET ($MUTPAR out of $MUTTOT)\n"
        MUTPAR2=$(cat log.txt | grep PROGRESS | tail -n1 | cut -d'(' -f2 | cut -d' ' -f1)
        progrBar $MUTPAR2 $MUTTOT
        #echo -ne ""\\r
        sleep $SLEEPTIME
        echo -e "\033[3A"
    done
elif [ "$(ps aux | grep "./run.sh -mut" | wc -l )" -gt "1" ]; then
    if [ "$(ps aux | grep "run.sh -mut" | grep "bin/bash" | cut -d'-' -f2 | cut -d' ' -f1)" == "mut" ]; then
        MUTNAME=$(ps aux | grep "run.sh -mut" | grep "bin/bash" | cut -d'-' -f2 | cut -d' ' -f2 | cut -d'.' -f2)
        MUTTOT=$(find $MUTATION_FOLDER -name "$MUTNAME.java" | grep "/mut/" | wc -l)
    fi
    PIDRUN=$(pgrep "run.sh")
    while [ "$(ps aux | grep "./run.sh -mut" | wc -l )" -gt "1" ]; do
        MUTLOSE=$(cat result.txt | grep "MUT (true)" | grep "Success perc. (0)" | wc -l)
        MUTNOFO=$(cat result.txt | grep "MUT (true)" | grep "Total Success perc. (N.A.) METHOD-NOT-FOUND!" | wc -l)
        #MUTLOSE=$(($MUTLOSE/2))
        MUTGET=$(cat result.txt | grep "MUT (true)" | grep "Success perc. (100)" | wc -l)
        MUTERR=$(cat errors.txt | grep "ERROR -> " | wc -l)
        MUTPAR=$(cat log.txt | grep "MUT" | tail -n1 | cut -d' ' -f2)
        echo -ne "FAIL: $MUTLOSE - NOTFOUND: $MUTNOFO ERROR: $MUTERR SUCC: $MUTGET ($MUTPAR out of $MUTTOT)\n"
        progrBar $MUTPAR $MUTTOT
        #echo -ne ""\\r
        sleep $SLEEPTIME
        echo -e "\033[2A"
    done
fi
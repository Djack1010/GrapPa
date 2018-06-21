#!/bin/bash
SCRIPTPATH=$PWD

MUTATION_FOLDER=$(cat config.txt | grep "MUTATION_FOLDER" | cut -d"=" -f2)
if [ ! -d "$MUTATION_FOLDER" ]; then
    echo "ERROR: Set the MUTATION_FOLDER variable in config.txt! Exiting..."
    exit
fi

MUTTOT=$( ls $MUTATION_FOLDER | wc -l )
MUTLOSE=$(cat result.txt | grep "MUT (true)" | grep "Success perc. (0)" | wc -l)
MUTNOFO=$(cat result.txt | grep "MUT (true)" | grep "Total Success perc. (N.A.) METHOD-NOT-FOUND!" | wc -l)
MUTGET=$(cat result.txt | grep "MUT (true)" | grep "Success perc. (100)" | wc -l)
MUTERR=$(cat errors.txt | grep "ERROR -> " | wc -l)
MUTPAR=$(($MUTGET+$MUTLOSE+$MUTNOFO))
echo "FAIL: $MUTLOSE - NOTFOUND: $MUTNOFO ERROR: $MUTERR SUCC: $MUTGET ($MUTPAR out of $MUTTOT)"
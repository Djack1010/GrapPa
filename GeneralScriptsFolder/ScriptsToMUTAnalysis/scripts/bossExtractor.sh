#!/bin/bash
SCRIPTPATH_ORIGIN="$( cd "$(dirname "$0")" ; pwd -P )"
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )/.."
CHANGE=6000

if [[ -z $1 ]]; then
    echo "Set ERROR to look for"
    exit
else
    ERRORNAME=$1
    rm -rf $SCRIPTPATH/$ERRORNAME
    mkdir $SCRIPTPATH/$ERRORNAME
fi

if [[ -z $2 ]]; then
    echo "Set LOCKNUM"
    exit
else
    LOCKNUM=$2
fi
echo "LOCKNUM set to $LOCKNUM"

if [[ -z $3 ]]; then
    SKIP=0
else
    echo "SKIPPING temp_parAnalysis creation"
    SKIP=1
fi

echo "Starting BOSS operation"

rm -f $SCRIPTPATH/FilesToExtract_$ERRORNAME.txt
rm -f /var/lock/lockCounter$LOCKNUM
touch /var/lock/lockCounter$LOCKNUM

if [ "$SKIP" -eq 0 ]; then
    rm -dr temp_partAnalysis
    mkdir temp_partAnalysis
    COUNTERFILE=0
    COUNTEROCC=0
    COUNTERLINE=0
    COUNTERTOT=0
    for B in $SCRIPTPATH/*_Analysis.txt; do
        LINE=$(wc -l < $B)
        COUNTERTOT=$(($COUNTERTOT+$LINE))
    done

    for A in $SCRIPTPATH/*_Analysis.txt; do
        while read l; do
            if [[ $l == "--------MUT"* ]]; then
                COUNTEROCC=$(($COUNTEROCC+1))
            fi
            if [ $COUNTEROCC -eq $CHANGE ]; then
                COUNTERFILE=$(($COUNTERFILE+1))
                COUNTEROCC=0
                echo $l >> $SCRIPTPATH/temp_partAnalysis/temp_"$COUNTERFILE"_Analysis.txt
            else
                echo $l >> $SCRIPTPATH/temp_partAnalysis/temp_"$COUNTERFILE"_Analysis.txt
            fi
            COUNTERLINE=$(($COUNTERLINE+1))
            PER=$(bc <<< "scale = 2; ($COUNTERLINE / $COUNTERTOT) * 100")
            echo -ne "$PER % lines analyzed"\\r     
        done < $A
    done
fi

COMMAND=""
echo "Starting multiprocessing..."
for T in $SCRIPTPATH/temp_partAnalysis/*; do
    COMMAND="$COMMAND $SCRIPTPATH_ORIGIN/mutExtractor.sh $T $ERRORNAME $LOCKNUM &"
done
COMMAND="$COMMAND wait"
eval $COMMAND

rm -f /var/lock/lockCounter$LOCKNUM

echo "Ending BOSS operation"

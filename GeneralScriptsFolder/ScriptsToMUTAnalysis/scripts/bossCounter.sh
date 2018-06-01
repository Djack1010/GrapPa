#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )/.."
CHANGE=6000

if [[ -z $1 ]]; then
    SKIP=0
else
    echo "SKIPPING temp_parAnalysis creation"
    SKIP=1
fi

echo "Starting BOSS operation"

rm -f /var/lock/lockCounter
touch /var/lock/lockCounter

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

rm -f $SCRIPTPATH/Counter.txt
touch $SCRIPTPATH/Counter.txt
COMMAND=""
echo "Starting multiprocessing..."
for T in $SCRIPTPATH/temp_partAnalysis/*; do
    COMMAND="$COMMAND $SCRIPTPATH/mutCounter.sh $T show &"
done
COMMAND="$COMMAND wait"
eval $COMMAND
echo "SORTING Counter.txt"

sort -nr -t_ -k4 $SCRIPTPATH/Counter.txt -o $SCRIPTPATH/Counter.txt

rm -f /var/lock/lockCounter

echo "Ending BOSS operation"

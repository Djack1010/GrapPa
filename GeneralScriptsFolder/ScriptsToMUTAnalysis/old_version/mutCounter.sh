#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )/.."
MAX=9000
if [[ -z $1 ]] || ! [[ -f $1 ]]; then
    echo "Analysis file not set, exiting..."
    exit
else
    ANALFILE=$1
fi

if [[ -z $2 ]]; then
    SHOW=0
else
    SHOW=1
fi

#echo "STARTING COUNTING by $ANALFILE..."
COUNTLIN=0
COUNTTOT=$(wc -l < $ANALFILE)
BOOL=0
while read l; do
    case "$BOOL" in
        "0")
            if [[ $(echo $l | grep "FAILED") ]] ; then
                TEMPLINE="FAILED"
                BOOL=1
                #printf "Failed --->"
            elif [[ $(echo $l | grep "Caused an ERROR") ]] ; then
                TEMPLINE="ERROR"
                BOOL=3
                #printf "Failed --->"
            fi
            ;;
        "1")
            if [[ $l == "expected"* ]]; then
                #PARTl=$( echo $l | sed -r 's/_/X/g' | sed -r 's/ //g' )
                #PARTl=$( echo "${PARTl//<*>/<XXX>butwas<YYY>}" )
                #PARTl=$( echo $PARTl | sed -r 's/ //g' )
                TEMPLINE=$TEMPLINE"_expected<XXX>butwas<YYY>"
                BOOL=2
                #printf " OK ---> "
            elif  [[ $l == "null"* ]]; then
                TEMPLINE=$TEMPLINE"_null"
                BOOL=2
                #printf " OK ---> "
            fi
            ;;
        "2")
            if [[ $l = \junit* ]] || [[ $l = \java* ]]; then
                PARTl=$( echo $l | cut -d':' -f1 )
                if [[ $PARTl = *"@"* ]]; then
                    PARTl=$( echo $PARTl | cut -d'@' -f1 )
                fi
                TEMPLINE=$TEMPLINE"_"$PARTl
                (
                    flock 200
                    ERRSTORED=$( cat $SCRIPTPATH/Counter.txt | grep -e "$TEMPLINE"_ )
                    if [[ $ERRSTORED ]] ; then
                        NUM=$( echo $ERRSTORED | cut -d'_' -f4 )
                        if [ $NUM -eq $MAX ] ; then
                            BOOL=0
                            #echo "REACHED MAX!"
                            continue
                        fi
                        NUM=$(($NUM+1))
                        sed -i "/$ERRSTORED/d" $SCRIPTPATH/Counter.txt
                        echo $TEMPLINE"_"$NUM >> $SCRIPTPATH/Counter.txt
                        #echo "UPDATED!"
                    else
                        echo $TEMPLINE"_1" >> $SCRIPTPATH/Counter.txt
                        #echo "NEW!"
                    fi
                ) 200>/var/lock/lockCounter
                BOOL=0
            fi
            ;;
        "3")
            if [[ $l = \junit* ]] || [[ $l = \java* ]]; then
                if [[ $l = *":"* ]]; then
                    PARTl=$( echo $l | cut -d':' -f1 )
                    TEMPLINE=$TEMPLINE"_"$PARTl
                elif [[ $l = *" "* ]]; then
                    PARTl=$( echo $l | cut -d' ' -f1 )
                    TEMPLINE=$TEMPLINE"_"$PARTl
                else
                    TEMPLINE=$TEMPLINE"_"$l
                fi
                TEMPLINE=$TEMPLINE"_NUSlot"
                #echo $TEMPLINE
                (
                    flock 200
                    ERRSTORED=$( cat $SCRIPTPATH/Counter.txt | grep -e "$TEMPLINE"_ )
                    if [[ $ERRSTORED ]] ; then
                        NUM=$( echo $ERRSTORED | cut -d'_' -f4 )
                        if [ $NUM -eq $MAX ] ; then
                            BOOL=0
                            #echo "REACHED MAX!"
                            continue
                        fi
                        NUM=$(($NUM+1))
                        sed -i "/$ERRSTORED/d" $SCRIPTPATH/Counter.txt
                        echo $TEMPLINE"_"$NUM >> $SCRIPTPATH/Counter.txt
                        #echo "UPDATED!"
                    else
                        echo $TEMPLINE"_1" >> $SCRIPTPATH/Counter.txt
                        #echo "NEW!"
                    fi
                ) 200>/var/lock/lockCounter
                BOOL=0
            fi
            ;;
        *)
            echo "ERROR, exiting..."
            exit
            ;;
    esac
    COUNTLIN=$(($COUNTLIN+1))
    PER=$(bc <<< "scale = 2; ($COUNTLIN / $COUNTTOT) * 100")
    if [ $SHOW -gt 0 ]; then
        echo -ne "$PER % lines analyzed"\\r 
    fi
done < $ANALFILE
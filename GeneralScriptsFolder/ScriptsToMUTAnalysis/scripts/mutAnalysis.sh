#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )/.."

if [[ -z $1 ]]; then
    echo "ERROR, set output file, exiting..."
    exit
else
    ANALFILE="$SCRIPTPATH/$1"
    #echo $ANALFILE
fi

if [[ -z $2 ]]; then
    echo "ERROR, set input folder, exiting..."
    exit
else
    INPUTFOLDER="$2"
    #echo $ANALFILE
fi

rm -f $ANALFILE

echo "STARTING ANALYSIS..."
for F in $SCRIPTPATH/$INPUTFOLDER; do
    echo "---> Analysing $F"
    if [ -d "${F}" ]; then #FOR each runned test
        if [ -d $F/test_report ]; then
            cd $F/test_report
        else
            continue
        fi
        cd $F/test_report
        COUNTLIN=0
        COUNTTOT=$(ls | wc -l)
        for S in $F/test_report/*; do #for each mutant
            #echo "# Mutant $S"
            if [ -d "${S}" ]; then
                for T in $S/*; do #for each test
                    if [ -f "${T}" ]; then
                        if [[ $(cat $T | grep "Failures: 0, Errors: 0,") ]]; then
                            #echo "Removing file $T because no failures/errors in it"
                            #cat $T
                            rm $T
                        else
                            MUTNUMB=${S##*/}
                            FOLDNUMB=${F##*/}
                            echo "--------MUT $MUTNUMB from $FOLDNUMB--------" >> $ANALFILE
                            COUNTER=0
                            BOOL=0
                            while read p; do
                                if [ $BOOL -gt 0 ]; then
                                    if [ -z "$p" ]; then
                                        BOOL=0
                                        continue
                                    else
                                        echo $p >> $ANALFILE
                                        continue
                                    fi
                                fi
                                if [[ $(echo $p | grep "FAILED") ]] || [[ $(echo $p | grep "ERROR") ]] ; then
                                    COUNTER=$(($COUNTER+1))
                                    echo "### $COUNTER ###" >> $ANALFILE
                                    echo $TEMPl1 >> $ANALFILE
                                    echo $p >> $ANALFILE
                                    BOOL=1
                                else                                    
                                    TEMPl1=$p                                
                                fi
                            done < $T
                            if [ $COUNTER -eq 0 ]; then
                                echo "### $COUNTER ###" >> $ANALFILE
                                echo "NO ERROR/FAILURE ENCOUNTERED" >> $ANALFILE
                                echo "TEST FAILED DUE TIME LIMIT (killed because overlimit)" >> $ANALFILE
                            fi
                        fi
                    fi
                done
            fi
            if [ -z "$(ls $S)" ]; then
                #echo "Folder $S is empty"
                rm -d $S
            fi
            COUNTLIN=$(($COUNTLIN+1))
            PER=$(bc <<< "scale = 2; ($COUNTLIN / $COUNTTOT) * 100")
            echo -ne "$PER % lines analyzed"\\r 
        done
    fi
done

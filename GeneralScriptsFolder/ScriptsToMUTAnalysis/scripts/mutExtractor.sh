#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )/.."

if [[ -z $1 ]] || ! [[ -f $1 ]]; then
    echo "Analysis file not set, exiting..."
    exit
else
    ANALFILE=$1
fi

if [[ -z $2 ]]; then
    echo "Set ERROR to look for"
    exit
else
    ERRORNAME=$2
    mkdir -p $SCRIPTPATH/$ERRORNAME
fi

if [[ -z $3 ]]; then
    echo "Set NAME_FOLDER (Example: nameFolder_AOR, nameFolder_LVR, etc...)"
    exit
else
    NAMEFOLDERPR=$3
fi

if [[ -z $4 ]]; then
    echo "Set LOCKNUM, exiting..."
    exit
else
    LOCKNUM=$4
fi


COUNTLIN=0
COUNTTOT=$(wc -l < $ANALFILE)
BOOL=0
while read l; do
    case "$BOOL" in
        "0")
            if [[ $(echo $l | grep "MUT TESTonMUT") ]] ; then
                BOOL=1
                MUTNUM=$( echo $l | cut -d' ' -f2 | cut -d'T' -f4 )
                MUTLOC=$( echo $l | cut -d'_' -f2 | cut -d'-' -f1)
            fi
            ;;
        "1")
            if [[ $(echo $l | grep "FAILED") ]] ; then
                BOOL=0
            elif [[ $(echo $l | grep "$ERRORNAME") ]] ; then
                FOLDER=$SCRIPTPATH/$ERRORNAME/$MUTNUM"_"$MUTLOC
                if [ -d "$FOLDER" ]; then
                    BOOL=0
                    continue
                else
                    mkdir $FOLDER
                fi
                (
                    flock 200
                    echo "MutNum $MUTNUM from folder $MUTLOC" >> $SCRIPTPATH/FilesToExtract_$ERRORNAME.txt
                ) 200>/var/lock/lockCounter$LOCKNUM
                VAL=0
                cd $SCRIPTPATH/"$NAMEFOLDERPR"_$MUTLOC/mutants/$MUTNUM
                while [[ "$VAL" -eq 0 ]]; do
                    TEMP=$(ls )
                    if [ -d "$TEMP" ]; then
                        cd $TEMP
                    elif [[ $TEMP = *".java" ]]; then
                        MUTFILE=$PWD/$TEMP
                        NAME=$TEMP
                        VAL=1
                    else
                        echo "EXITING WITH ERROR, empty/unexpected folder"
                        echo ".java file not found in $PWD"
                        exit
                    fi
                done
                ORCODE=$( find $SCRIPTPATH/src/ -name "$NAME")
                if [ -z "$ORCODE" ]; then
                    echo "EXITING WITH ERROR, original code $NAME not found..."
                    exit
                fi
                mkdir $FOLDER/mut
                cp $MUTFILE $FOLDER/mut/$NAME
                cp $ORCODE $FOLDER/$NAME
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
    echo -ne "$PER % lines analyzed"\\r 
done < $ANALFILE
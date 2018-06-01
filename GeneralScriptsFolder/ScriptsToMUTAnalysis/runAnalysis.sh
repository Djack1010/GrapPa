#!/bin/bash
SCRIPTPATH=$PWD

function runMutAnalysis {
    echo "ARGUMENTS REQUIRED: OutputFileName InputFolderName"
    echo "OutputFileName -> suggested *_Analysis.txt"
    read ARG1
    echo "OutputFileName: $ARG1"
    read ARG2
    echo "InputFolderName: $ARG2"
    echo "Starting script? [Y/n]"
    read -n1 START
    echo -e "\n"
    if [ "$START" == "n" ]; then
        runMutAnalysis
    else
        $SCRIPTPATH/scripts/mutAnalysis.sh $ARG1 $ARG2
    fi
}

function runBossCounter {
    echo "ARGUMENTS REQUIRED: [ FolderDivision ]"
    echo "FolderDivision -> If set, reuse temp_partAnalysis folder (should exist...)"
    echo "Reuse FolderDivision? [Y/n]"
    read -n1 FDIV
    if [ "$FDIV" == "n" ]; then
        echo "Reuse Set: NO"
    else
        echo "Reuse Set: YES"
    fi
    echo -e "\nStarting script? [Y/n]"
    read -n1 START
    echo -e "\n"
    if [ "$START" == "n" ]; then
        runBossCounter
    else
        if [ "$FDIV" == "n" ]; then
            $SCRIPTPATH/scripts/bossCounter.sh
        else
            $SCRIPTPATH/scripts/bossCounter.sh ReFold
        fi
    fi
}

function runBossExtractor {
    echo "ARGUMENTS REQUIRED: Error LockNum [ FolderDivision ]"
    echo "FolderDivision -> If set, reuse temp_partAnalysis folder (should exist...)"
    read ARG1
    echo "Error: $ARG1"
    read ARG2
    echo "LockNum: $ARG2"
    echo "Reuse FolderDivision? [Y/n]"
    read -n1 FDIV
    if [ "$FDIV" == "n" ]; then
        echo "Reuse Set: NO"
    else
        echo "Reuse Set: YES"
    fi
    echo -e "\nStarting script? [Y/n]"
    read -n1 START
    echo -e "\n"
    if [ "$START" == "n" ]; then
        runBossExtractor
    else
        if [ "$FDIV" == "n" ]; then
            $SCRIPTPATH/scripts/bossExtractor.sh $ARG1 $ARG2
        else
            $SCRIPTPATH/scripts/bossExtractor.sh $ARG1 $ARG2 ReFold
        fi
    fi
}

echo "Which script run?"
echo -e "0 \t--> mutAnalysis"
echo -e "1 \t--> bossCounter"
echo -e "2 \t--> boosExtractor"
echo -e "e \t--> Exit"
read -n1 REQUEST3
echo -e "\n"
case $REQUEST3 in
    '0' )
        runMutAnalysis
    ;;
    '1' )
        runBossCounter
    ;;
    '2' )
        runBossExtractor
    ;;
    'e' )
        echo "Exiting..."
        exit
    ;;
    * )
        echo "Invalid input, exiting..."
        UsageInfo
        exit
    ;;
esac
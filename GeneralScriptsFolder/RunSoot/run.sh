#!/bin/bash
#Created by Giacomo Iadarola
#v1.0 - 22/05/18

function UsageInfo {
    echo "USAGE: ./run.sh [Option Argument]"
    echo "-targ CLASS: run on a single CLASS file"
    echo "-mut CLASS: run on a mutated CLASS file"
    echo "-allclasses: run on all class file in SOURCE_ANALYSIS_FOLDER"
    exit
}

function AnalysisResult {
    if [ -f $1 ]; then
        while read l ;
        do
            if [[ $l = "RESULT "* ]]; then
                echo $l >> $SCRIPTPATH/temp_result.txt
            else
                continue
            fi
        done < $1
        rm $1
        cat $SCRIPTPATH/temp_result.txt >> $SCRIPTPATH/result.txt
        rm $SCRIPTPATH/temp_result.txt
    else
        echo "ERROR! result.txt not found, exiting..."
        exit
    fi
}

function MutationHandler {
    rm -f $SCRIPTPATH/temp_MUT.txt
    MUTFOLDER=$1
    MUTNAME="$2.java"
    TOREPLACE=$( find $SOURCE_ANALYSIS_FOLDER -name "$MUTNAME")
    if [ -z "$TOREPLACE" ]; then
        echo "EXITING WITH ERROR, $MUTNAME TO REPLACE NOT FOUND..."
        exit
    fi
    find $MUTFOLDER -name "$MUTNAME" >> $SCRIPTPATH/temp_MUT.txt
    if ! [[ $(cat $SCRIPTPATH/temp_MUT.txt) ]]; then
        echo "EXITING WITH ERROR, $MUTNAME TO REPLACE NOT FOUND..."
        exit
    fi 

    mutArray[0]=0
    while read l; do
        if [ $(echo $l | grep "/mut/") ]; then
            continue
        fi
        mutArray[0]=$((${mutArray[0]}+1))
        mutArray[${mutArray[0]}]=$(echo $l | sed "s/\/$MUTNAME//g")
    done < $SCRIPTPATH/temp_MUT.txt
    rm -f $SCRIPTPATH/temp_MUT.txt
    for (( n=1; n<=${mutArray[0]}; n++ )); do  
        MUTFOLDER=${mutArray[$n]}
        if [ ! -f $MUTFOLDER/mut/$MUTNAME ] || [ ! -f $MUTFOLDER/$MUTNAME ]; then
            echo "EXITING WITH ERROR, $MUTFOLDER DOES NOT CONTAIN MUT and ORIGINAL FILES..."
            exit
        fi 
        if [ $(diff $TOREPLACE $MUTFOLDER/$MUTNAME) ]; then
            echo "EXITING WITH ERROR, $MUTFOLDER ORIGINAL FILE DIVERSE BY $TOREPLACE..."
            diff $TOREPLACE $MUTFOLDER/$MUTNAME 
            exit
        fi
        echo "Deleting $TOREPLACE..."
        rm $TOREPLACE
        cp $MUTFOLDER/mut/$MUTNAME $TOREPLACE
        
        cd $PROJECT_FOLDER
        mvn clean
        mvn compile

        $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
            SourceCode.MainCPG -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
            -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER $JPACK.$DEFAULT_MAIN_CLASS -mutationClass $JPACK.$2
        rm $TOREPLACE
        cp $MUTFOLDER/$MUTNAME $TOREPLACE
        echo "Restored $TOREPLACE!"
    done  
}

function LoopFolder {
    if [ -z "$1" ]; then
        echo "ERROR! Argument not set for LoopFolder, exiting..."
        exit
    fi
    for JavaFile in $1; do
        rm -rf sootOutput
        THISCLASS=$(echo $JavaFile | cut -d"." -f1 | sed 's/\//./g' )
        echo "STARTING ANALYSIS FOR $JavaFile"
        $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
            SourceCode.MainCPG -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
            -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER $JPACK.$DEFAULT_MAIN_CLASS -targetClass $JPACK.$THISCLASS 2>> $SCRIPTPATH/errors.txt 1>> $SCRIPTPATH/result.txt
        echo "ENDING ANALYSIS FOR $JavaFile"
        if [[ $(cat $SCRIPTPATH/result.txt | grep "Soot finished") ]] ; then
            if [ -f $SCRIPTPATH/analysisResult.sh ]; then
                AnalysisResult $SCRIPTPATH/result.txt
            else
                echo "ERROR! analysisResult script not found!"
                exit
            fi
        else
            echo "EXIT WITH ERROR, check errors.txt file"
            AnalysisResult $SCRIPTPATH/result.txt
            exit
        fi
    done
}

SCRIPTPATH=$PWD
rm -f $SCRIPTPATH/result.txt
rm -f $SCRIPTPATH/errors.txt

if [ ! -f config.txt ]; then
    echo "ERROR! File config.txt not found! Exiting..."
    exit
fi

PROJECT_FOLDER=$(cat config.txt | grep "PROJECT_FOLDER" | cut -d"=" -f2)
SOURCE_ANALYSIS_FOLDER=$(cat config.txt | grep "SOURCE_ANALYSIS_FOLDER" | cut -d"=" -f2)
PACKAG_ANALYSIS_FOLDER=$(cat config.txt | grep "PACKAG_ANALYSIS_FOLDER" | cut -d"=" -f2)
MUTATION_FOLDER=$(cat config.txt | grep "MUTATION_FOLDER" | cut -d"=" -f2)
DEFAULT_MAIN_CLASS=$(cat config.txt | grep "DEFAULT_MAIN_CLASS" | cut -d"=" -f2)
CLASS_FOLDER=$(cat config.txt | grep "CLASS_FOLDER" | cut -d"=" -f2)
SOOT_JAR=$(cat config.txt | grep "SOOT_JAR" | cut -d"=" -f2)
JAVA_LIBS=$(cat config.txt | grep "JAVA_LIBS" | cut -d"=" -f2)
JAVA7_HOME=$(cat config.txt | grep "JAVA7_HOME" | cut -d"=" -f2)

if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "ERROR: Set the PROJECT_FOLDER variable in config.txt! Exiting..."
    exit
elif [ ! -d "$SOURCE_ANALYSIS_FOLDER" ]; then
    echo "ERROR: Set the SOURCE_ANALYSIS_FOLDER variable in config.txt! Exiting..."
    exit
elif [ ! -d "$MUTATION_FOLDER" ]; then
    echo "ERROR: Set the MUTATION_FOLDER variable in config.txt! Exiting..."
    exit
elif [ ! -d "$SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER" ]; then
    echo "ERROR: Set the PACKAG_ANALYSIS_FOLDER variable in config.txt! Exiting..."
    exit
elif [ ! -d "$JAVA7_HOME" ]; then
    echo "ERROR: Set the JAVA7_HOME variable in config.txt! Exiting..."
    exit
elif [ ! -f "$SOOT_JAR" ]; then
    echo "ERROR: Set the SOOT_JAR variable in config.txt! Exiting..."
    exit
elif [ -z "$JAVA_LIBS" ]; then
    echo "ERROR: Set the JAVA_LIBS variable in config.txt! Exiting..."
    exit
elif [ -z "$DEFAULT_MAIN_CLASS" ]; then
    echo "ERROR: Set the DEFAULT_MAIN_CLASS variable in config.txt! Exiting..."
    exit
fi

MYCP_JAVA=".:$CLASS_FOLDER:$SOOT_JAR" #:$PROJECT_FOLDER
JPACK=$(echo "$PACKAG_ANALYSIS_FOLDER" | sed 's/\//./g' )
if [[ $JPACK == .* ]]; then
    JPACK=${JPACK#"."}
fi

if [ ! -d "$CLASS_FOLDER" ]; then
    echo "ERROR: Set the CLASS_FOLDER variable in config.txt! Exiting..."
    exit
fi

#java -cp $MYCP_JAVA SourceCode.MainCPG -cp $SOURCE_ANALYSIS_FOLDER -pp -w SourceCode.test
if [ -z "$1" ]; then
    cd $PROJECT_FOLDER
    mvn clean
    mvn compile
    $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
        SourceCode.MainCPG -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
        -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER $JPACK.$DEFAULT_MAIN_CLASS
elif [[ "$1" == "-allclasses" ]]; then
    cd $PROJECT_FOLDER
    mvn clean
    mvn compile
    cd $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER
    LoopFolder "*/*.java"
    LoopFolder "*.java"    
elif [[ "$1" == "-mut" ]]; then
    if [ -z "$2" ]; then
        UsageInfo
    else
        MutationHandler $MUTATION_FOLDER $2
    fi
elif [[ "$1" == "-targ" ]]; then
    if [ -z "$2" ]; then
        UsageInfo
    else
        cd $PROJECT_FOLDER
        mvn clean
        mvn compile
        $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
            SourceCode.MainCPG -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
            -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER $JPACK.$DEFAULT_MAIN_CLASS -targetClass $JPACK.$2
    fi
elif [[ "$1" == "-help" ]]; then
    UsageInfo
else
    UsageInfo
fi
echo "ENDING run.sh SCRIPT"
#-cp /home/djack/Desktop/Test_Folder/RunSoot/InputClasses/java.lang.NullPointerException/3_3 -pp -w AnnotationUtils

#!/bin/bash
#Created by Giacomo Iadarola
#v1.0 - 22/05/18

function UsageInfo {
    echo "USAGE: ./run.sh [ OP CLASS [ OP2 METHOD] | -allclasses [ -graph2vec TOOLNAME] | -cpgtofile ]"
    echo -e "Available OP = -targ | -mut"
    echo -e "Available OP2 = -meth"
    echo -e "\t-targ CLASS: run on a single CLASS file"
    echo -e "\t-mut CLASS: run on a mutated CLASS file"
    echo -e "\t-meth METHOD: run on a specific METHOD"
    echo -e "\t-allclasses: run on all class files in SOURCE_ANALYSIS_FOLDER"
    echo -e "\t-cpgtofile: run on all class files in SOURCE_ANALYSIS_FOLDER and store graph in nedoFolder/graphDB"
    echo -e "\t-graph2vec TOOLNAME: print graph on file as input format for TOOLNAME (see Readme for available TOOLNAME options)"
    echo -e "TOOLNAME list separated by semicolon : (Example: -graph2vec struc2vec:CGMM )"
    exit
}

function preAnalysisResult {
    if [[ $(cat $SCRIPTPATH/result.txt | grep "Soot finished") ]] ; then
        if [ -f $SCRIPTPATH/analysisResult.sh ]; then
            AnalysisResult $SCRIPTPATH/result.txt
        else
            echo "ERROR! analysisResult script not found!"
            exit
        fi
    else
        if [[ $(cat $SCRIPTPATH/result.txt | grep "ERROR -> OutOfMemoryError: ") ]] ; then
            echo "Out of Memory Error catched, check errors.txt file"
            echo "Out of Memory Error catched, check errors.txt file" >> $SCRIPTPATH/log.txt
            cat $SCRIPTPATH/result.txt | grep "ERROR -> OutOfMemoryError: " >> $SCRIPTPATH/errors.txt
            AnalysisResult $SCRIPTPATH/result.txt
        elif [[ $(cat $SCRIPTPATH/result.txt | grep "ERROR -> NullPointerException: ") ]] ; then
            echo "Null Pointer Exception catched, check errors.txt file"
            echo "Null Pointer Exception catched, check errors.txt file" >> $SCRIPTPATH/log.txt
            cat $SCRIPTPATH/result.txt | grep "ERROR -> NullPointerException: " >> $SCRIPTPATH/errors.txt
            AnalysisResult $SCRIPTPATH/result.txt
        else
            echo "EXIT WITH ERROR, check errors.txt file"
            #AnalysisResult $SCRIPTPATH/result.txt
            exit
        fi
    fi
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
        rm -f $1
        if [ -f $SCRIPTPATH/temp_result.txt ]; then 
            cat $SCRIPTPATH/temp_result.txt >> $SCRIPTPATH/result.txt
            rm -f $SCRIPTPATH/temp_result.txt
        fi
    else
        echo "ERROR! result.txt not found, exiting..."
        exit
    fi
}

function MutationHandler {
    rm -f $SCRIPTPATH/temp_MUT.txt
    MUTFOLDER=$1
    if [ $(echo $2 | grep ".") ]; then
        MUTNAME_temp=$( echo ${2##*.} )
        MUTNAME="$MUTNAME_temp.java"
    else
        MUTNAME="$2.java"
    fi
    if [ -z $3 ]; then
        MAINFILE="MainCPG"
    else
        MAINFILE=$3
    fi
    TOREPLACE=$( find $SOURCE_ANALYSIS_FOLDER -name "$MUTNAME")
    if [ -z "$TOREPLACE" ]; then
        echo "EXITING WITH ERROR#1, $MUTNAME TO REPLACE NOT FOUND..."
        exit
    fi
    find $MUTFOLDER -name "$MUTNAME" >> $SCRIPTPATH/temp_MUT.txt
    if ! [[ $(cat $SCRIPTPATH/temp_MUT.txt) ]]; then
        echo "$MUTNAME HAS NO MUTANTS..." >> $SCRIPTPATH/log.txt
        return
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
        if [ ! -z "$MUTPAR" ]; then
            MUTPAR=$(($MUTPAR+1))
        fi  
        echo "MUTANT $n out of ${mutArray[0]} at $(date)" >> $SCRIPTPATH/log.txt
        MUTFOLDER=${mutArray[$n]}
        if [ ! -f $MUTFOLDER/mut/$MUTNAME ] || [ ! -f $MUTFOLDER/$MUTNAME ] || [ -z "$(diff $MUTFOLDER/mut/$MUTNAME $MUTFOLDER/$MUTNAME)" ]; then
            echo "EXITING WITH ERROR, $MUTFOLDER DOES NOT CONTAIN MUT and ORIGINAL FILES (OR they are identical)"
            exit
        fi 

        rm -f $SCRIPTPATH/tempJava
        LINED=$(diff $MUTFOLDER/mut/$MUTNAME $MUTFOLDER/$MUTNAME | grep -v "^---" | grep -v "^[<>]" | cut -d'c' -f1)
        if [ "$LINED" == "$(diff $MUTFOLDER/mut/$MUTNAME $MUTFOLDER/$MUTNAME | grep -v "^---" | grep -v "^[<>]" | cut -d'c' -f2)" ]; then
            tac $MUTFOLDER/$MUTNAME | tail -n $LINED >> $SCRIPTPATH/tempJava
            COUNTERLINE=0
            while read l; do
                if [[ $( echo $l | egrep 'private|public|protected'  ) ]]; then
                    TEMP=$( echo $l | cut -d'(' -f1 )
                    TARGETMETHOD=$( echo ${TEMP##* } )
                    COUNTERLINE=$(($LINED-$COUNTERLINE))
                    TARGETMETHOD="$TARGETMETHOD:$COUNTERLINE"
                    break
                else
                    COUNTERLINE=$(($COUNTERLINE+1))
                fi
            done < $SCRIPTPATH/tempJava
            rm $SCRIPTPATH/tempJava
            if [ -z "$TARGETMETHOD" ]; then
                echo "EXITING WITH ERROR, MUTATED METOD NOT FOUND"
                exit
            fi
        fi

        if [ $(diff $TOREPLACE $MUTFOLDER/$MUTNAME) ]; then
            echo "EXITING WITH ERROR, $MUTFOLDER ORIGINAL FILE DIVERSE BY $TOREPLACE..."
            diff $TOREPLACE $MUTFOLDER/$MUTNAME 
            exit
        fi

        if [ ! -z $LOOK4METH ] && [ "$LOOK4METH" != "$( echo "$TARGETMETHOD" | cut -d':' -f1 )" ]; then
            continue
        fi

        echo "TARGET METHOD: $TARGETMETHOD" >> $SCRIPTPATH/log.txt

        #----------------------------------------------------------------------------------------
        #CODE TO RESTORE COMPUTATION FROM SPECIFIC MUTANTS - FOR LONG (SUSPENDED) COMPUTATION
        #----------------------------------------------------------------------------------------
        #if [ -z $EMERGENCYVAR ] && [ "TARGET METHOD: $TARGETMETHOD" != "TARGET METHOD: readArgumentIndex:327" ]; then
            #echo "Skipped, already computed..." >> $SCRIPTPATH/log.txt
            #continue
        #elif [ -z $EMERGENCYVAR ] && [ "TARGET METHOD: $TARGETMETHOD" == "TARGET METHOD: readArgumentIndex:327" ]; then
            #EMERGENCYVAR="SET"
            #echo "Skipped, not work..." >> $SCRIPTPATH/log.txt
            #continue
        #fi
        #if [ -z $EMERGENCYVAR ] && [ "MUTANT $n out of ${mutArray[0]}" != "MUTANT 241 out of 441" ]; then
            #echo "Skipped, already computed..." >> $SCRIPTPATH/log.txt
            #continue
        #elif [ -z $EMERGENCYVAR ] && [ "MUTANT $n out of ${mutArray[0]}" == "MUTANT 241 out of 441" ]; then
            #EMERGENCYVAR="SET"
            #echo "Skipped, not work..." >> $SCRIPTPATH/log.txt
            #continue
        #fi
        #----------------------------------------------------------------------------------------

        #mvn -f $PROJECT_FOLDER clean
        #mvn -f $PROJECT_FOLDER compile
        echo "GENERATING ORIGINAL GRAPH for $MUTNAME - $TARGETMETHOD"
        $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
            SourceCode.$MAINFILE -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
            -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER -mainClass $JPACK.$DEFAULT_MAIN_CLASS -targetClass $JPACK.$2 -targetMethod $TARGETMETHOD $GRAPH2VECTOOL 2>> $SCRIPTPATH/errors.txt 1>> $SCRIPTPATH/result.txt
        
        preAnalysisResult

        echo "Deleting $TOREPLACE..." >> $SCRIPTPATH/log.txt
        rm $TOREPLACE
        cp $MUTFOLDER/mut/$MUTNAME $TOREPLACE
        
        #mvn -f $PROJECT_FOLDER clean
        #mvn -f $PROJECT_FOLDER compile
        echo "GENERATING MUTATED GRAPH for $MUTNAME - $TARGETMETHOD"
        $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
            SourceCode.$MAINFILE -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
            -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER -mainClass $JPACK.$DEFAULT_MAIN_CLASS -mutationClass $JPACK.$2 -targetMethod $TARGETMETHOD $GRAPH2VECTOOL 2>> $SCRIPTPATH/errors.txt 1>> $SCRIPTPATH/result.txt
        rm $TOREPLACE
        cp $MUTFOLDER/$MUTNAME $TOREPLACE
        echo "Restored $TOREPLACE!"  >> $SCRIPTPATH/log.txt
        preAnalysisResult
    done  
}

SCRIPTPATH=$PWD
rm -rf $SCRIPTPATH/backup
mkdir $SCRIPTPATH/backup
if [ -f $SCRIPTPATH/result.txt ]; then
    mv $SCRIPTPATH/result.txt $SCRIPTPATH/backup/result.txt
fi
if [ -f $SCRIPTPATH/errors.txt ]; then
    mv $SCRIPTPATH/errors.txt $SCRIPTPATH/backup/errors.txt
fi
if [ -f $SCRIPTPATH/log.txt ]; then
    mv $SCRIPTPATH/log.txt $SCRIPTPATH/backup/log.txt
fi

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
#TODO not implemented yet, all files go to DBGRAPH in NEDO folder
DB_GRAPH_FOLDER=$(cat config.txt | grep "DB_GRAPH_FOLDER" | cut -d"=" -f2)

if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "ERROR: Set the PROJECT_FOLDER variable in config.txt! Exiting..."
    exit
elif [ ! -d "$SOURCE_ANALYSIS_FOLDER" ]; then
    echo "ERROR: Set the SOURCE_ANALYSIS_FOLDER variable in config.txt! Exiting..."
    exit
elif [ ! -d "$DB_GRAPH_FOLDER" ]; then
    echo "ERROR: Set the DB_GRAPH_FOLDER variable in config.txt! Exiting..."
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

mvn -f $PROJECT_FOLDER clean
mvn -f $PROJECT_FOLDER compile

if [ ! -d "$CLASS_FOLDER" ]; then
    echo "ERROR: Set the CLASS_FOLDER variable in config.txt! Exiting..."
    exit
fi

if [ "$#" -eq 0 ]; then
    $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
        SourceCode.MainCPG -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
        -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER -mainClass $JPACK.$DEFAULT_MAIN_CLASS 2>> $SCRIPTPATH/errors.txt 1>> $SCRIPTPATH/result.txt
    preAnalysisResult
else
    myArray=( "$@" )
    n=0
    MODE=""
    while [ $n -lt $# ]; do
        if [[ "${myArray[$n]}" == "-allclasses" ]]; then
            MODE="a"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-cpgtofile" ]]; then
            MODE="f"
            n=$(($n+1))
        elif [[ "${myArray[$n]}" == "-mut" ]]; then
            MODE="m"
            n=$(($n+1))
            if [ -z "${myArray[$n]}" ]; then
                UsageInfo
            else
                JCLASS=${myArray[$n]}
                n=$(($n+1))
            fi
        elif [[ "${myArray[$n]}" == "-targ" ]]; then
            MODE="t"
            n=$(($n+1))
            if [ -z "${myArray[$n]}" ]; then
                UsageInfo
            else
                JCLASS=${myArray[$n]}
                n=$(($n+1))
            fi
        elif [ "${myArray[$n]}" == "-graph2vec" ];then
            n=$(($n+1))
            if [ -z "${myArray[$n]}" ]; then
                UsageInfo
            else
                GRAPH2VECTOOL="-graph2vec ${myArray[$n]}"
                n=$(($n+1))
            fi
        elif [ "${myArray[$n]}" == "-meth" ];then
            n=$(($n+1))
            if [ -z "${myArray[$n]}" ]; then
                UsageInfo
            else
                LOOK4METH=${myArray[$n]}
                n=$(($n+1))
            fi
        elif [[ "${myArray[$n]}" == "-help" ]]; then
            UsageInfo
        else
            UsageInfo
        fi       
    done
fi

if [ "$MODE" == "a" ]; then
    CLASPAR=0
    CLASTOT=$(grep -o "\.java" <<< $JAVAFILELIST | wc -l)
    MUTTOT=$( ls $MUTATION_FOLDER | wc -l )
    MUTPAR=0
    JAVAFILELIST=$(find $SOURCE_ANALYSIS_FOLDER$PACKAG_ANALYSIS_FOLDER -name "*.java")
    CLASTOT=$(grep -o "\.java" <<< $JAVAFILELIST | wc -l)
    SOURCEPATH4REGEX=$(echo $SOURCE_ANALYSIS_FOLDER$PACKAG_ANALYSIS_FOLDER | sed "s/\//\\\\\//g")
    for JavaFile in $JAVAFILELIST; do
        echo "--------> PROGRESS: MUTANTS ($MUTPAR out of $MUTTOT) - CLASS ($CLASPAR out of $CLASTOT) <--------"
        echo "PROGRESS: MUTANTS ($MUTPAR out of $MUTTOT) - CLASS ($CLASPAR out of $CLASTOT)" >> $SCRIPTPATH/log.txt
        rm -rf sootOutput
        #rm -rf $SOURCE_ANALYSIS_FOLDER/sootOutput
        JavaFileNEW=$(echo $JavaFile | sed "s/$SOURCEPATH4REGEX\///g")
        THISCLASS=$(echo $JavaFileNEW | cut -d"." -f1 | sed 's/\//./g' )
        echo "STARTING ANALYSIS FOR $JavaFileNEW at $(date)"  >> $SCRIPTPATH/log.txt
        MutationHandler $MUTATION_FOLDER $THISCLASS #$GRAPH2VECTOOL
        echo "ENDING ANALYSIS FOR $JavaFileNEW at $(date)"  >> $SCRIPTPATH/log.txt
        CLASPAR=$(($CLASPAR+1))
    done
elif [ "$MODE" == "f" ]; then
    CLASPAR=0
    CLASTOT=$(grep -o "\.java" <<< $JAVAFILELIST | wc -l)
    MUTTOT=$( ls $MUTATION_FOLDER | wc -l )
    MUTPAR=0
    JAVAFILELIST=$(find $SOURCE_ANALYSIS_FOLDER$PACKAG_ANALYSIS_FOLDER -name "*.java")
    CLASTOT=$(grep -o "\.java" <<< $JAVAFILELIST | wc -l)
    SOURCEPATH4REGEX=$(echo $SOURCE_ANALYSIS_FOLDER$PACKAG_ANALYSIS_FOLDER | sed "s/\//\\\\\//g")
    for JavaFile in $JAVAFILELIST; do
        echo "--------> PROGRESS: MUTANTS ($MUTPAR out of $MUTTOT) - CLASS ($CLASPAR out of $CLASTOT) <--------"
        echo "PROGRESS: MUTANTS ($MUTPAR out of $MUTTOT) - CLASS ($CLASPAR out of $CLASTOT)" >> $SCRIPTPATH/log.txt
        rm -rf sootOutput
        #rm -rf $SOURCE_ANALYSIS_FOLDER/sootOutput
        JavaFileNEW=$(echo $JavaFile | sed "s/$SOURCEPATH4REGEX\///g")
        THISCLASS=$(echo $JavaFileNEW | cut -d"." -f1 | sed 's/\//./g' )
        echo "STARTING ANALYSIS FOR $JavaFileNEW at $(date)"  >> $SCRIPTPATH/log.txt
        MutationHandler $MUTATION_FOLDER $THISCLASS MainCPGtoFile
        echo "ENDING ANALYSIS FOR $JavaFileNEW at $(date)"  >> $SCRIPTPATH/log.txt
        CLASPAR=$(($CLASPAR+1))
    done
elif [ "$MODE" == "m" ]; then 
    MutationHandler $MUTATION_FOLDER $JCLASS #$GRAPH2VECTOOL  
elif [ "$MODE" == "t" ]; then
    $JAVA7_HOME/bin/java -cp $MYCP_JAVA \
        SourceCode.MainCPG -p cg all-reachable:true -w -no-bodies-for-excluded -full-resolver \
        -cp $SOURCE_ANALYSIS_FOLDER:$JAVA_LIBS -process-dir $SOURCE_ANALYSIS_FOLDER/$PACKAG_ANALYSIS_FOLDER -mainClass $JPACK.$DEFAULT_MAIN_CLASS -targetClass $JPACK.$JCLASS $GRAPH2VECTOOL 2>> $SCRIPTPATH/errors.txt 1>> $SCRIPTPATH/result.txt
    preAnalysisResult
fi

echo "ENDING run.sh SCRIPT"
exit

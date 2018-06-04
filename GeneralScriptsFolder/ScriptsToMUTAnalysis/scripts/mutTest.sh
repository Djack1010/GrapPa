#!/bin/bash
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"


function handleInsDel {
    if [ "$1" == "rep" ]; then
        cp $TOREPLACE $SCRIPTPATH/temp_folder 
        rm $TOREPLACE
        cp $MUTPATH $TOREPLACE
    elif [ "$1" == "res" ]; then
        rm $TOREPLACE
        cp $SCRIPTPATH/temp_folder/$MUTNAME $TOREPLACE
        rm $SCRIPTPATH/temp_folder/$MUTNAME
    else
        echo "ERROR in handleInsDel, exiting..."
        exit
    fi
}
if [[ -z $1 ]]; then
    echo "Mutation type not set, exiting..."
    exit
elif [ "$1" != "AOR" ] && [ "$1" != "LOR" ] && [ "$1" != "COR" ] && [ "$1" != "ROR" ] && [ "$1" != "SOR" ] && [ "$1" != "EVR" ] && [ "$1" != "LVR" ]; then
    echo "Mutation type unkown, exiting..."
    exit
else
    MUTTY=$1
    echo $MUTTY
fi

#rm -fdr src
#rm -fdr JAIL
#tar xzf $SCRIPTPATH/commons-lang3-3.4-src.tar.gz
#cd $SCRIPTPATH/commons-lang3-3.4-src
#ls | grep -v src | xargs rm
#cd $SCRIPTPATH
#mv $SCRIPTPATH/commons-lang3-3.4-src/src $SCRIPTPATH
#rm -d $SCRIPTPATH/commons-lang3-3.4-src
#mkdir -p $SCRIPTPATH/JAIL
#mv $SCRIPTPATH/src/test/java/org/apache/commons/lang3/time/DateUtilsTest.java $SCRIPTPATH/JAIL
#mv $SCRIPTPATH/src/test/java/org/apache/commons/lang3/time/FastDatePrinterTimeZonesTest.java $SCRIPTPATH/JAIL
#mv $SCRIPTPATH/src/test/java/org/apache/commons/lang3/math/FractionTest.java $SCRIPTPATH/JAIL

rm -fdr src
cd ..
cp -R src $SCRIPTPATH
cd $SCRIPTPATH

mvn compile "-DmutEn=true" "-DmutType=$MUTTY"
mvn clean
mvn compile "-DmutEn=false" "-DmutType=NONE"
mkdir -p $SCRIPTPATH/temp_folder
mkdir -p $SCRIPTPATH/test_report
COUNTER=0
TOT=$(ls $SCRIPTPATH/mutants | wc -l)
MUTNAME=""

for D in $SCRIPTPATH/mutants/*; do
    NAV=$D
    while [ -d $NAV ]; do
        cd $NAV
        NAV=$(ls)
    done

    if [[ $(ls $SCRIPTPATH/temp_folder) ]]; then
        handleInsDel res
    fi

    MUTNAME=$NAV
    MUTPATH="$PWD/$NAV"
    MUTNUMB=${D##*/}
    echo "----------------------------------------------------------"
    echo "-------------------STARTING $MUTNAME ---------------------"
    echo "----------------------------------------------------------"
    
    COUNTER=$(($COUNTER+1))
    PER=$(bc <<< "scale = 2; ($COUNTER / $TOT) * 100")
    echo "------------- INFO : $PER % mutants analyzed -------------"
    echo "----------------------------------------------------------"

    TOREPLACE=$( find $SCRIPTPATH/src/ -name "$MUTNAME")
    if [ -z "$TOREPLACE" ]; then
        echo "SKIPPING FILE, $MUTNAME TO REPLACE NOT FOUND..."
        continue
    fi
    
    handleInsDel rep

    cd $SCRIPTPATH

    MUTNAME_temp=$(echo $MUTNAME | cut -d'.' -f1 )
    if [ -d $SCRIPTPATH/target/classes ]; then
        COMPILED=$( find $SCRIPTPATH/target/classes -name "$MUTNAME_temp".class )
    fi
    
    if [ -z "$COMPILED" ]; then
        echo "$MUTNAME_temp.class TO DELETE NOT FOUND..."
    else
        rm $COMPILED
    fi

    if [[ ! $(find $SCRIPTPATH/src -name "$MUTNAME_temp"Test*) ]]; then
        echo "$MUTNAME has no test, skipping..."
        handleInsDel res
        continue
    fi

    #majorAnt clean.classes
    mvn compile  "-DmutEn=false" "-DmutType=NONE"
    mvn test "-DtarTest=$MUTNAME_temp"

    #TEST=$(ls $SCRIPTPATH/target/temp_test-reports)
    #mv $SCRIPTPATH/target/temp_test-reports/$TEST $SCRIPTPATH/test_report/TEST-"$COUNTER"
    if [ -d "$SCRIPTPATH/target/surefire-reports" ]; then
        rm -f $SCRIPTPATH/target/surefire-reports/*.xml
        cp -R $SCRIPTPATH/target/surefire-reports $SCRIPTPATH/test_report/TESTonMUT"$MUTNUMB"
        rm -dr $SCRIPTPATH/target/surefire-reports 
    else
        echo "ERROR! TEST FOLDER NOT FOUND, skipping $MUTNAME..."
        handleInsDel res
        mvn clean
        mvn compile  "-DmutEn=false" "-DmutType=NONE"
        continue
    fi

    handleInsDel res
     
    echo "-----------------------------------------------------"
    echo "-------------------END $MUTNAME----------------------"
    echo "-----------------------------------------------------"
    #COUNTER=$(($COUNTER+1))
    #PER=$(bc <<< "scale = 2; ($COUNTER / $TOT) * 100")
    #echo -ne "$PER % mutants analyzed"\\r 
done
rm -dr $SCRIPTPATH/temp_folder

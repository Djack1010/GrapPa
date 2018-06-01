#!/bin/bash
SCRIPTPATH=$PWD
rm -f $SCRIPTPATH/temp_find.txt
find $SCRIPTPATH -name '*.java' >> $SCRIPTPATH/temp_find.txt
TOT=$(wc -l < $SCRIPTPATH/temp_find.txt)
ACT=0
while read javaFile;
do 
    sed -i 's/@Override//g' $javaFile
    PER=$(bc <<< "scale = 2; ($ACT / $TOT) * 100")
    ACT=$(($ACT+1))
    echo -ne "$PER % files analyzed"\\r
done < $SCRIPTPATH/temp_find.txt
echo "CHECKING JAVA FILES FOR OVERRIDE LINES"
while read javaFile;
do
    if [[ $(cat $javaFile | grep "@Override" ) ]]; then 
        echo "$javaFile contains @Override!"
    fi
done < $SCRIPTPATH/temp_find.txt
rm $SCRIPTPATH/temp_find.txt
echo "END"
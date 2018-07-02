#!/bin/bash
SCRIPTLOC="$( cd "$(dirname "$0")" ; pwd -P )"

for f in $(find $SCRIPTLOC -name '*.adjlist' -or -name '*.emb' -or -name '*.txt'); do 
    echo "Removing $f"
    rm $f; 
done

rm -rf $SCRIPTLOC/graph/info
rm -f $SCRIPTLOC/graph/err.log

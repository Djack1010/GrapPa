#!/bin/bash
SCRIPTLOC="$( cd "$(dirname "$0")" ; pwd -P )"

for f in $(find $SCRIPTLOC -name '*.json'); do 
    echo "Removing $f"
    rm $f; 
done

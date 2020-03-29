#!/bin/bash

PWD=`dirname $0`
cd $PWD
cd ..

CLASS_PATH=.:./conf

for jar in lib/*.jar
do
    CLASS_PATH=$CLASS_PATH:$jar
done

OPTS=""
if [ "$1" = "-debug" ]; then
    OPTS="$OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=$2,suspend=n"
    shift
    shift
fi

if [ "$1" = "" ]; then
    echo "Usage: run.sh [-debug port] <class name>"
    exit
fi

CLASS=$1
shift

echo "java $OPTS -cp $CLASS_PATH $CLASS $@"

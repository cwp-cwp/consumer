#!/bin/bash
PWD=`dirname $0`
NOW=`date +%Y%m%d%H%M%S`
cd $PWD
cd ..
CLASS=com.puzek.consumer.ConsumerApplication
cmd=`bin/run.sh $CLASS`
$cmd
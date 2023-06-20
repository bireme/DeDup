#!/bin/sh

DD_DIR=/home/javaapps/DeDup/jetty-base
cd $DD_DIR || exit

../jetty-home-11.0.14/bin/jetty.sh stop

sleep 120s

cd -

exit $ret

#!/bin/sh

restart=25
while [ $restart -eq 25 ]
do
     /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/bin/java -ea -DportName=/dev/ttyACM0 -Dmonocle.input.touchRadius=3 -Djava.net.useSystemProxies=true -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -jar /home/pi/NetBeansProjects/DrawAndCut/dist/DrawAndCut.jar
     restart=$?
done

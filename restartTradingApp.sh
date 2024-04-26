#!/bin/bash 


ps -ef| grep [s]pring-boot | tr -s " " | cut -d " " -f 3 | xargs -t kill -9
cd /Users/omkartamboli/work/myProjects/ibkrTradingApp

export PATH=$PATH:/usr/local/bin:/usr/local/Cellar/maven/3.6.1/bin
export M2_HOME=/usr/local/Cellar/maven/3.6.1
export M2=/usr/local/Cellar/maven/3.6.1/bin
#export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/

echo "Running IBKR APP now"

mvn clean install spring-boot:run

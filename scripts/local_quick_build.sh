#!/usr/bin/env bash

set -e

export MAVEN_OPTS="$MAVEN_OPTS -XX:MaxPermSize=1024m -Xmx2048m"
export JAVA_OPTS="$JAVA_OPTS -XX:MaxPermSize=1024m -Xmx2048m"
export JAVA_HOME=`/usr/libexec/java_home -v 1.7`
echo
java -version
echo
mvn -version
echo

# to run from specific module use argument in quotes "-rf mockserver-war"
mvn clean install $1 -Djava.security.egd=file:/dev/urandom
#!/bin/bash

# Runs WordSensor on the large Guardian dataset.

src=java
jars=jars/opennlp-tools-1.6.0.jar

os="`uname`"

echo Your OS is $os

# If not linux, we assume Windows.
case $os in
	Linux*) path=$src:$jars:jars/jwnl.jar:jars/commons-logging-1.2.jar ;;
	*) path="$src;$jars;jars.jwnl.jar;jars/commons-logging-1.2.jar" ;;
esac

java -cp $path WordSenseTrainer Dataset false

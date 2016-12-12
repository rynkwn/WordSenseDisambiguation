#!/bin/bash

# Runs WordSensor on the large Guardian dataset.

src=java
jars=jars/opennlp-tools-1.6.0.jar

os="`uname`"

echo Your OS is $os

# If not linux, we assume Windows.
case $os in
	Linux*) path=$src:$jars:jars/* ;;
	*) path="$src;$jars;jars/*" ;;
esac

java -Xmx1000g -cp $path WordSensor Dataset false

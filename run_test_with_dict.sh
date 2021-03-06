#!/bin/bash

# Runs WordSensor on the small test dataset.
# Also uses the dictionary definitions of the ambiguous word.

src=java
jars=jars/opennlp-tools-1.6.0.jar

os="`uname`"

echo Your OS is $os

# If not linux, we assume Windows.
case $os in
	Linux*) path=$src:$jars:jars/* ;;
	*) path="$src;$jars;jars/*" ;;
esac

java -Xmx6g -cp $path WordSensor TestData true

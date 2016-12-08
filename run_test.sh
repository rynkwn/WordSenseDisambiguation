#!/bin/bash

# A script to run WordSensor on data in the TestData set.

src=java
jars=jars/opennlp-tools-1.6.0.jar

os="`uname`"

echo Your OS is $os

# If not linux, we assume Windows.
case $os in
	Linux*) path=$src:$jars ;;
	*) path=$src;$jars ;;
esac

java -cp $path WordSensor TestData

#!/bin/bash

java=java/*.java
jars=jars/opennlp-tools-1.6.0.jar

os="`uname`"

echo Your OS is $os

# If not linux, we assume Windows.
case $os in
	Linux*) jars=$jars:jars/jwnl.jar ;;
	*) jars=$jars;jars/jwnl.jar ;;
esac

javac -cp $jars $java

#!/bin/bash

java=java/*.java
jars=jars/opennlp-tools-1.6.0.jar

os="`uname`"

echo Your OS is $os

# If not linux, we assume Windows.
case $os in
	Linux*) path=$src:$jars ;;
	*) path=$src;$jars ;;
esac

javac -cp $jars $java

#!/bin/bash

if [ -f Harmonium.jar ]
then
rm Harmonium.jar
fi

if [ -e bin ]
then
rm -rf bin
fi

mkdir bin
find . -iname *.java > srclist
javac @srclist -target 1.5 -d bin @classpathlist
cp org/dazeend/harmonium/*.png bin/org/dazeend/harmonium/
cp org/dazeend/harmonium/*.gif bin/org/dazeend/harmonium/
cp org/dazeend/harmonium/*.mpg bin/org/dazeend/harmonium/
jar cf Harmonium.jar -C bin .

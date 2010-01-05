#!/bin/bash
rm Harmonium.jar
rm -rf bin
mkdir bin
find . -iname *.java > srclist
javac @srclist -target 1.5 -d bin @classpathlist
cp org/dazeend/harmonium/*.png bin/org/dazeend/harmonium/
cp org/dazeend/harmonium/*.gif bin/org/dazeend/harmonium/
cp org/dazeend/harmonium/*.mpg bin/org/dazeend/harmonium/
jar cf Harmonium.jar -C bin .

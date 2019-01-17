#!/bin/bash

echo "===========Compiling Zimulator Core=============="

HERE="$PWD"

cd Zimulator

mvn clean install

if [ $? == 1 ]
then
    echo Maven failed. Aborting.
    exit
fi

#
# Re-make the jar, putting a classpath in the manifest. Stupid.
#

cd target
mkdir TMP
cd TMP
jar xf ../Zimulator.jar
cp "$HERE"/Zimulator/src/main/java/com/ibm/Zimulator/Zimulator/Manifest.txt ./
jar cfm "$HERE"/Zimulator.jar Manifest.txt *

cd "$HERE"
echo "Created jar:"
ls -l Zimulator.jar

#!/bin/bash

echo "===========Compiling Zimulator Command-line Interface=============="

HERE="$PWD"

cd ZimCLI

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
jar xf ../ZimCLI.jar
cp "$HERE"/ZimCLI/src/main/java/com/ibm/Zimulator/ZimCLI/Manifest.txt ./
jar cfm "$HERE"/ZimCLI.jar Manifest.txt *

cd "$HERE"
echo "Created jar:"
ls -l ZimCLI.jar

#!/bin/bash

echo "==================" TravelTimeTool "====================="
rm -f TravelTimeTool/*.class
rm -f TravelTimeTool.jar
export CLASSPATH="SmallAux.jar"
javac -Xlint:unchecked TravelTimeTool/*.java
if [ $? == 1 ]
then
    echo Compilation failed. Aborting.
    exit
fi
echo "Compilation successful; building jar."
jar cfm TravelTimeTool.jar TravelTimeTool/Manifest.txt TravelTimeTool/*.class
if [ $? == 1 ]
then
    echo jar production failed. Aborting.
    exit
fi
echo "TravelTimeTool.jar produced."

echo "======================" Running TravelTimeTool.jar  "======================"
java -jar TravelTimeTool.jar

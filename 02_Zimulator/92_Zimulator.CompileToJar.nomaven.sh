#!/bin/bash

echo "===========Compiling SmallAux auxiliary library=============="

HERE="$PWD"
SRCDIR=Zimulator/src/main/java/com/ibm/Zimulator/

echo "Cleaning..."
rm -f Zimulator.jar $SRCDIR/Zimulator/*.class
echo "Compiling..."
export CLASSPATH=""
cd $SRCDIR
javac -cp "$HERE/SmallAux.jar" -Xlint:unchecked Zimulator/*.java 
if [ $? == 1 ]
then
    echo Compilation failed. Aborting.
    exit
fi

echo "Building jar..."
cd "$HERE"/Zimulator/src/main/java/
jar cfm "$HERE"/Zimulator.jar com/ibm/Zimulator/Zimulator/Manifest.txt com/ibm/Zimulator/Zimulator/*.class
if [ $? == 1 ]
then
    echo jar production failed. Aborting.
    exit
fi
cd "$HERE"
echo "Finished:"
ls -l Zimulator.jar

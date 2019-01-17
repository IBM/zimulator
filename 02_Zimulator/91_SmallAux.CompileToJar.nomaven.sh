#!/bin/bash

echo "===========Compiling SmallAux auxiliary library=============="

HERE="$PWD"
SRCDIR=SmallAux/src/main/java/com/ibm/Zimulator/

echo "Cleaning..."
rm -f SmallAux.jar $SRCDIR/SmallAux/*.class $SRCDIR/SmallAux/*/*.class

echo "Compiliing..."
export CLASSPATH=""
cd $SRCDIR
javac -Xlint:unchecked SmallAux/*.java SmallAux/*/*.java
if [ $? == 1 ]
then
    echo Compilation failed. Aborting.
    exit
fi
echo "Building jar..."
cd "$HERE"/SmallAux/src/main/java/
jar cfm "$HERE"/SmallAux.jar com/ibm/Zimulator/SmallAux/Manifest.txt com/ibm/Zimulator/SmallAux/*.class com/ibm/Zimulator/SmallAux/*/*.class
if [ $? == 1 ]
then
    echo jar production failed. Aborting.
    exit
fi
cd "$HERE"
echo "Finished:"
ls -l SmallAux.jar

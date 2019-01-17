#!/bin/bash

echo "===========Compiling SmallAux auxiliary library=============="

HERE="$PWD"
SRCDIR=ZimCLI/src/main/java/com/ibm/Zimulator/

echo "Cleaning..."
rm -f ZimCLI.jar $SRCDIR/ZimCLI/*.class
echo "Compiling..."
export CLASSPATH=""
cd $SRCDIR
javac -cp "$HERE/SmallAux.jar:$HERE/Zimulator.jar" -Xlint:unchecked ZimCLI/*.java 
if [ $? == 1 ]
then
    echo Compilation failed. Aborting.
    exit
fi

echo "Building jar..."
cd "$HERE"/ZimCLI/src/main/java/
jar cfm "$HERE"/ZimCLI.jar com/ibm/Zimulator/ZimCLI/Manifest.txt com/ibm/Zimulator/ZimCLI/*.class
if [ $? == 1 ]
then
    echo jar production failed. Aborting.
    exit
fi
cd "$HERE"
echo "Finished:"
ls -l ZimCLI.jar
echo "Running:"
java -jar ZimCLI.jar

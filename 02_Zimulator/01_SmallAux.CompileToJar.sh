#!/bin/bash

echo "===========Compiling SmallAux auxiliary library=============="

HERE="$PWD"

cd SmallAux

mvn clean install

if [ $? == 1 ]
then
    echo Maven failed. Aborting.
    exit
fi

cd "$HERE"

echo "Created jar:"
cp -f SmallAux/target/SmallAux.jar ./
ls -l SmallAux.jar


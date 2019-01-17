#!/bin/bash

THRESH="08:35"

WATCH=PaxProto/75

echo "####### Running all in one go:"
java -jar ZimCLI.jar -v I=FiveRooms.v01.zim R=__FiveRooms.report.zo  | grep ___

echo "####### Running and then saving state:"
java -jar ZimCLI.jar -v I=FiveRooms.v01.zim  t_1="$THRESH" o="_until_$THRESH.zobj" R=__FiveRooms.report1.zo  | grep ___

echo "####### Restoring state and continuing:"
java -jar ZimCLI.jar -v i="_until_$THRESH.zobj" t_1=12:00 R=__FiveRooms.report2.zo  | grep ___

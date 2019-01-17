#!/bin/bash

java -jar TravelTimeTool.jar TTD 05:00,25:00,80 10,6000,40 out/madrid_08.zo | tee _TTD_0500-2500.csv

java -jar TravelTimeTool.jar TTD 07:00,09:00,4 10,6000,40 out/madrid_08.zo | tee _TTD_0700-0900.csv

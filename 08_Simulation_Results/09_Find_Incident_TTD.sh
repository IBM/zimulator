#!/bin/bash

java -jar TravelTimeTool.jar TTD 07:00,09:00,4 10,6000,40 out/madrid_08_incident.zo | tee _TTD_0700-0900_incident.csv

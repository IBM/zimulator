#!/bin/bash

OUTF=out/madrid_08.zo

echo DEL_Pf_L10_1...
./05_FindPlatformOccupancy DEL_Pf_L10_1 "$OUTF" > _Occupancy_DEL_Pf_L10_1.csv
echo JVJ_Pf_L10_1...
./05_FindPlatformOccupancy JVJ_Pf_L10_1 "$OUTF" > _Occupancy_JVJ_Pf_L10_1.csv
echo PDC_Pf_L1_0...
./05_FindPlatformOccupancy PDC_Pf_L1_0 "$OUTF" > _Occupancy_PDC_Pf_L1_0.csv
echo CDC_Bn_L10_0...
./05_FindTrainOccupancyAt CDC_Bn_L10_0 "$OUTF" > _Occupancy_CDC_Bn_L10_0.csv
echo PÓP_Bn_L2_1...
./05_FindTrainOccupancyAt PÓP_Bn_L2_1  "$OUTF" > _Occupancy_PÓP_Bn_L2_1.csv
echo Trk_L10_1_DEL_JVJ...
./05_FindTrainOccupancyAt Trk_L10_1_DEL_JVJ "$OUTF" > _Occupancy_Trk_L10_1_DEL_JVJ.csv
echo Train_proto/L3/2...
./05_FindRunningTrainOccupancy Train_proto/L3/2 "$OUTF" >_Occupancy_Train_proto_L3_2.csv
echo Train_proto/L10/129...
./05_FindRunningTrainOccupancy Train_proto/L10/129 "$OUTF" >_Occupancy_Train_proto_L10_129.csv

exit


SimpleGnuplot  _Occupancy_CDC_Bn_L10_0 400 25200,28800,0,0 "*Time of Day [s]" "Pax" 'CDC\_Bn\_L10\_0' pdf,eps \
	       / _Occupancy_CDC_Bn_L10_0.csv 1 4 with boxes  linewidth 5 col 008
SimpleGnuplot  _Occupancy_PÓP_Bn_L2_1 400 25200,28800,0,0 "*Time of Day [s]" "Pax" 'P'\'O'P\_Bn\_L2\_1' pdf,eps \
	       / _Occupancy_PÓP_Bn_L2_1.csv 1 4 with boxes  linewidth 5 col 008

SimpleGnuplot  _Occupancy_PDC_Pf_L1_0 400 25200,28800,0,0 "*Time of Day [s]" "Pax" 'PDC\_Pf\_L1\_0' pdf,eps \
	       / _Occupancy_PDC_Pf_L1_0.csv 1 4 with lines linewidth 5 col 080
SimpleGnuplot  _Occupancy_DEL_Pf_L10_1 400 25200,28800,0,0 "*Time of Day [s]" "Pax" 'DEL\_Pf\_L10\_1' pdf,eps \
	       / _Occupancy_DEL_Pf_L10_1.csv 1 4 with lines linewidth 5 col 080

SimpleGnuplot  _Occupancy_Trk_L10_1_DEL_JVJ 400 25200,28800,0,0 "*Time of Day [s]" "Pax" 'Trk\_L10\_1\_DEL\_JVJ' pdf,eps \
	       / _Occupancy_Trk_L10_1_DEL_JVJ.csv 1 4 with boxes  linewidth 5 col 008

SimpleGnuplot  _Occupancy_Train_proto_L3_2 400 0,0,0,0 "*Time of Day [s]" "Pax" 'Train\_proto/L3/2' pdf,eps \
	       / _Occupancy_Train_proto_L3_2.csv 1 4 with boxes  linewidth 5 col 800

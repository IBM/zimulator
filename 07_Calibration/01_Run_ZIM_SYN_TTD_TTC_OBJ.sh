

fil="0500-0900"
BUX="05:00,09:00,16"
T1="09:00"

ZOUT="out/madrid_""$fil"".zo"

echo "============================ZIM: " Run simulation
sleep 2

java  -jar ZimCLI.jar -razo zl=1 z=60 t_1="$T1" I=ZimInput/00_StaticTypes.zim I=ZimInput/_01_Stations.zim \
      I=ZimInput/_02_Tracks.zim  I=ZimInput/_03_Paths.zim  I=ZimInput/_04_Schedules.zim  \
      I=ZimInput/_05_Demand.zim  R="$ZOUT"

echo "============================SYN: " Produce Synthetic data:
sleep 2

#
# Synthetic 'ground truth' will be SLOWER. Therefore, calibration will reduce walking speed.
#

java -jar TravelTimeTool.jar SYN 100 "$ZOUT"  | tee _test_SYN_."$fil".csv 

echo "============================TTC: " Make comparison:

java -jar TravelTimeTool.jar TTC "$BUX" -300,300,40 _test_SYN_."$fil".csv "$ZOUT"  | tee _test_TTC_."$fil".csv 

# Bplot 80 50 3 1,2 _test_TTC_."$fil".csv
# SimpleGnuplot _test_TTC_."$fil" 300 0,0,0,0 '*$\Delta t$' 'frac' 'TTC' eps,pdf / _test_TTC_."$fil".csv 3 1 with lines col a00
# okular _test_TTC_."$fil".pdf

echo "============================TTD: " Show TTD:
sleep 2

java -jar TravelTimeTool.jar TTD "$BUX" 0,7200,25 "$ZOUT"  | tee _test_TTD_."$fil".csv 

# Bplot 80 50 3 1,2 _test_TTD_."$fil".csv
# SimpleGnuplot _test_TTD_."$fil" 300 0,0,0,0 '*$t$' 'frac' 'TTD' eps,pdf / _test_TTD_."$fil".csv 3 1 with lines col a00
# okular _test_TTD_."$fil".pdf

echo "============================OBJ: " Evaluate Φ " (as an example)"
sleep 2

java -jar TravelTimeTool.jar OBJ "$BUX" _test_SYN_."$fil".csv "$ZOUT" 


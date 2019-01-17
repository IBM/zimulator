

fil="0500-0900"
BUX="05:00,9:00,16"
T1="9:00"

ZOUT="out/madrid_Calibrated_""$fil"".zo"

echo "============================ZIM: " Run simulation

java  -jar ZimCLI.jar -razo zl=1 z=60 t_1="$T1" I=_00_StaticTypes.zim \
      I=ZimInput/_01_Stations.zim I=ZimInput/_02_Tracks.zim  \
      I=ZimInput/_03_Paths.zim  I=ZimInput/_04_Schedules.zim  \
      I=ZimInput/_05_Demand.zim  R="$ZOUT"

echo "============================TTC: " Make comparison:

java -jar TravelTimeTool.jar TTC "$BUX" -600,600,20 _test_SYN_."$fil".csv "$ZOUT"  | tee _PostCalib_TTC_."$fil".csv 

#Bplot 80 50 3 1,2 _PostCalib_TTC_."$fil".csv
#SimpleGnuplot _PostCalib_TTC_."$fil" 300 0,0,0,0 '*$\Delta t$' 'frac' 'TTC' eps,pdf / _PostCalib_TTC_."$fil".csv 3 1 with lines col a00
#okular _PostCalib_TTC_."$fil".pdf

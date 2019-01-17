# Calibration

## Travel-Time Tool

* Compilation:
```
$ ./TravelTimeTool.CompileToJar.sh
```
* Execution:
```
$ ./java -jar TravelTimeTool.jar
```
Documentation of the various modes mentioned below will be shown in the terminal.

## Use of Travel-Time Tool to:

* Generate a distribution of travel times given a simulation run:
```
$ java -jar TravelTimeTool.jar TTD
* Produce synthetic reference data (a synthetic `ground truth'):
```
$ java -jar TravelTimeTool.jar SYN 
```
* Compare the results of a simulation run with a reference set:
```
$ java -jar TravelTimeTool.jar TTC
```
* Calculate the value of the Objective function, indicating the
degree to which a simulation run result differs from a reference:
```
$ java -jar TravelTimeTool.jar OBJ
```

* Each of the above steps is demonstrated in sequence by running
```
$ ./01_Run_ZIM_SYN_TTD_TTC_OBJ.sh
```

## Calibration of parameters v_μ and v_σ of walking-time distribution

```
$ ./02_Run_Zim_Minimise_Objective.awk
```
The `_00_StaticTypes.zim` file will contain the v_μ and v_σ parameter values explored as calibration is performed.
When finished, the final values will remain therein.

#!/bin/bash
sparkcommand="../../../../spark-2.3.2-bin-hadoop2.6/bin/spark-submit --master spark://en4119507l.cidse.dhcp.asu.edu:7077 --class com.zishanfu.geosparksim.GeoSparkSim /hdd2/code/zishanfu/GeoSparkSim/GeoSparkSim/target/GeoSparkSim-1.0-SNAPSHOT-jar-with-dependencies.jar"
checksize="../../../../hadoop-2.6.5/bin/hadoop fs -du -s -h /vistrips/reports_*"

$sparkcommand -n 100000 -p 500 -r 120
sleep $sleepinterval


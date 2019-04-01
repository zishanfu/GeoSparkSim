#!/bin/bash
sparkcommand="../../../spark-2.3.2-bin-hadoop2.6/bin/spark-submit --master spark://en4119507l.cidse.dhcp.asu.edu:7077 --class GeoSparkSim /hdd2/code/zishanfu/GeoSparkSim/GeoSparkSim/target/GeoSparkSim-1.0-SNAPSHOT-jar-with-dependencies.jar"
checksize="../../../hadoop-2.6.5/bin/hadoop fs -du -s -h /vistrips/reports*"

hdfs_path="hdfs://en4119507l.cidse.dhcp.asu.edu:54310"

sleepinterval=60

number=100000
timestep=1
steps=600
partition=1000
repartition=120

echo "Spark timeTest "$steps" #####################################"

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


steps=1200
echo "Spark timeTest "$steps" #####################################"

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


steps=1800
echo "Spark timeTest "$steps" #####################################"

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n number -p partition -r repartition -s steps -t timestep -f hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


#!/bin/bash
sparkcommand="../../../spark-2.3.2-bin-hadoop2.6/bin/spark-submit --master spark://en4119507l.cidse.dhcp.asu.edu:7077 --class com.zishanfu.vistrips.App /hdd2/code/zishanfu/GeoSparkSim/target/VisTrips_v1-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
checksize="../../../hadoop-2.6.5/bin/hadoop fs -du -s -h /vistrips/reports_*"

hdfs_path="hdfs://en4119507l.cidse.dhcp.asu.edu:54310"
osm_path="/hdd2/code/zishanfu/map.osm"

sleepinterval=60

number=100000
timestamp=1
simulation=10
partition=1
partitionNum=192


simulation=20
echo "vehicle simulation "$simulation" minutes #####################################"

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

simulation=30
echo "vehicle simulation "$simulation" minutes #####################################"

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

simulation=40
echo "vehicle simulation "$simulation" minutes #####################################"

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10

$sparkcommand $number $timestamp $simulation $partition $hdfs_path $osm_path $partitionNum
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep 10


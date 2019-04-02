#!/bin/bash
sparkcommand="../../../spark-2.3.2-bin-hadoop2.6/bin/spark-submit --master spark://en4119507l.cidse.dhcp.asu.edu:7077 --class com.zishanfu.geosparksim.GeoSparkSim /hdd2/code/zishanfu/GeoSparkSim/target/GeoSparkSim-1.0-SNAPSHOT-jar-with-dependencies.jar"
checksize="../../../hadoop-2.6.5/bin/hadoop fs -du -s -h /geosparksim/reports*"

hdfs_path="hdfs://en4119507l.cidse.dhcp.asu.edu:54310"

sleepinterval=60

number=100000
timestep=1
steps=600
partition=1500
repartition=120

echo "Spark numberTest "$number" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


number=200000
echo "Spark numberTest "$number" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


number=300000
echo "Spark numberTest "$number" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh

number=200000
partition=3000
echo "Spark numberTest "$number" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

number=300000
partition=4500
echo "Spark numberTest "$number" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval






number=100000
timestep=1
steps=600
partition=1500
repartition=60

echo "Spark repartitionTest "$repartition" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


repartition=120
echo "Spark repartitionTest "$repartition" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


repartition=180
echo "Spark repartitionTest "$repartition" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


repartition=240
echo "Spark repartitionTest "$repartition" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


repartition=300
echo "Spark repartitionTest "$repartition" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh




number=100000
timestep=1
steps=600
partition=1500
repartition=120

echo "Spark timestepTest "$timestep" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


timestep=0.8
echo "Spark timestepTest "$timestep" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


timestep=0.6
echo "Spark timestepTest "$timestep" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval




number=100000
timestep=1
steps=600
partition=1500
repartition=120

echo "Spark timeTest "$steps" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


steps=1200
echo "Spark timeTest "$steps" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval


steps=1800
echo "Spark timeTest "$steps" #####################################"

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

$sparkcommand -c -d -n $number -p $partition -r $repartition -s $steps -t $timestep -f $hdfs_path
$checksize
sleep $sleepinterval

sh restart-spark.sh
sleep $sleepinterval

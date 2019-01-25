
#!/bin/bash
sparkcommand="../../spark-2.3.2-bin-hadoop2.6/bin/spark-submit --master spark://en4119507l.cidse.dhcp.asu.edu:7077 --class com.zishanfu.vistrips.App ../target/VisTrips_v1-0.0.1-SNAPSHOT.jar"
hdfs_path="hdfs://en4119507l.cidse.dhcp.asu.edu:54310"

sleepinterval=60
number=100000
timestamp=1
simulation=10
partition=5

$sparkcommand number timestamp simulation partition hdfs_path
sleep $sleepinterval
package com.zishanfu.vistrips;

import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;

import com.zishanfu.vistrips.tools.HDFSUtil;

/**
 * Hello world!
 *
 */
public class App 
{
	private static Logger LOG = Logger.getLogger(App.class);
	//static String resources = System.getProperty("user.dir") + "/src/test/resources";
	//static String resources = System.getProperty("user.dir") + "/src/test/resources";
	
	//number
	//timestamp
	//simulation time
	//partition time
	//hdfs path 10.218.111.177
	//osm path /home/zishanfu/Downloads/datasets/map.osm
	//partition num
    public static void main( String[] args )
    {	
    	SparkSession spark = SparkSession
    			  .builder()
    			  .master("local[*]")
    			  .appName("App")
    			  .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    	          .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
    			  .getOrCreate();
    	int total = Integer.parseInt(args[0]);
    	double timestamp = Double.parseDouble(args[1]);
    	int simTime = Integer.parseInt(args[2]);
    	int parTime = Integer.parseInt(args[3]);
    	String str = args[4];
    	String osm = args[5];
    	int partition = Integer.parseInt(args[6]);
    	LOG.warn(String.format("Total: %s, timestamp: %s, simulation time: %s, partition time: %s, partition used: %s", total, timestamp, simTime, parTime, partition));
//        new Jmap().runUI();
    	LOG.debug("Log4j appender configuration is successful !!");
    	HDFSUtil hdfs = new HDFSUtil(str);
    	JmapConsole simConsole = new JmapConsole(spark, hdfs, osm, partition);
    	simConsole.runGeneration(total);
    	//run simulation in certain time period(minutes)
    	simConsole.runSimulation(timestamp, simTime, parTime);
    }
}

package com.zishanfu.vistrips;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.spark.sql.SparkSession;

/**
 * Hello world!
 *
 */
public class App 
{
	private static Logger LOG = Logger.getLogger(App.class);
//	static Logger logger = LoggerFactory.getLogger(App.class);
	static String resources = System.getProperty("user.dir") + "/src/test/resources";
	
    public static void main( String[] args )
    {	
    	SparkSession spark = SparkSession
    			  .builder()
    			  .master("local[*]")
    			  .appName("App")
    			  .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    	          .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
    			  .getOrCreate();
    	
//        new Jmap().runUI(spark);
    	LOG.debug("Log4j appender configuration is successful !!");
    	new JmapConsole().run(resources, spark);
    }
}

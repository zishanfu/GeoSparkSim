package com.zishanfu.vistrips;

import org.apache.spark.graphx.Graph;
import org.apache.spark.graphx.VertexRDD;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;

import com.vividsolutions.jts.geom.Point;
import com.zishanfu.vistrips.map.OsmConverter;
import com.zishanfu.vistrips.network.Link;
import com.zishanfu.vistrips.network.Route;
import com.zishanfu.vistrips.path.ShortestPathFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
	SparkSession spark = SparkSession
			  .builder()
			  .master("local")
			  .appName("AppTest")
			  .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
	          .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
			  .getOrCreate();
	
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	//validGraphBuild();
        assertTrue( true );
    }
    
    public void validGraphBuild() {
    	//sink existing osm file
    	String resources = System.getProperty("user.dir") +"/src/test/resources";
//    	String osmpath = resources + "/customizedOSM.osm";
//    	System.out.println(osmpath);
//    	File osmFile = new File(osmpath);
//    	CompressionMethod compression = CompressionMethod.None;
//    	XmlReader reader = new XmlReader(osmFile, false, compression);
//    	reader.setSink(new OsmParquetSink(resources));
//    	reader.run();
    	Graph<Point, Link> graph = OsmConverter.convertToNetwork(spark, resources);
    	//OsmGraph graph = new OsmGraph(spark, resources);
    	RDD<Route> route = ShortestPathFactory.runDijkstra(graph, 4347874712L, 5662664861L);
    	double km = route.first().getDistanceInKilometer();
    	System.out.println(km);
    }
    
//    public void testGraphrequest() {
//    	//Parser OSM by bbx of highway 202, 60, 10, 101.
//    	//Node (lat: 33.4456525, lon: -111.9845294, 2699986556)
//    	//Node (lat: 33.3775038, lon: -111.8810206, 3268411287)
//    	GeoPosition p1 = new GeoPosition(33.4456525, -111.9845294);
//    	GeoPosition p2 = new GeoPosition(33.3775038, -111.8810206);
//    	String path = OsmParser.run(p1, p2);
//    	
//    	OsmGraph graph = new OsmGraph(spark, path);
//    	GeoPosition cp1 = graph.getClosestNode(p1);
//    	assertEquals(p1, cp1);
//    	
//    	//from chick-fil-A in mill ave (lat: 33.421714, lon: -111.940756)
//    	//to home (lat: 33.409893, lon: -111.919567)
//    	Route route = graph.fatestRouteRequest(33.421714, -111.940756,
//    			33.409893, -111.919567);
//    	double km = route.getDistanceInKilometer();
//    	System.out.println(km);
//    	
//    }
}

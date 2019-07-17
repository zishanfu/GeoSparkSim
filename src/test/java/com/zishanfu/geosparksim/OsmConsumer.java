package com.zishanfu.geosparksim;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OsmConsumer extends GeoSparkSimTestBase {
    @BeforeClass
    public static void onceExecutedBeforeAll()
    {

    }

    @AfterClass
    public static void tearDown()
    {
        sc.stop();
    }


    @Test
    public void testReadRDD()
    {

    }
}

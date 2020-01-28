package com.zishanfu.geosparksim.interaction.handler;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Microscopic;
import com.zishanfu.geosparksim.generation.CreateVehicles;
import com.zishanfu.geosparksim.interaction.components.AttentionDialog;
import com.zishanfu.geosparksim.interaction.components.SelectionAdapter;
import com.zishanfu.geosparksim.model.Intersect;
import com.zishanfu.geosparksim.model.Link;
import com.zishanfu.geosparksim.model.MOBILVehicle;
import com.zishanfu.geosparksim.model.StepReport;
import com.zishanfu.geosparksim.model.TrafficLight;
import com.zishanfu.geosparksim.model.Vehicle;
import com.zishanfu.geosparksim.osm.OsmConverter;
import com.zishanfu.geosparksim.osm.OsmLoader;
import com.zishanfu.geosparksim.osm.ReportHandler;
import com.zishanfu.geosparksim.osm.RoadNetwork;
import com.zishanfu.geosparksim.osm.RoadNetworkReader;
import com.zishanfu.geosparksim.osm.RoadNetworkWriter;
import com.zishanfu.geosparksim.osm.VehicleHandler;
import com.zishanfu.geosparksim.tools.Distance;
import com.zishanfu.geosparksim.tools.HDFSUtil;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.viewer.GeoPosition;
import scala.collection.JavaConverters;
import scala.collection.Seq;

public class GenerateBtnHandler implements ActionListener {
    private final Logger LOG = Logger.getLogger(GenerateBtnHandler.class);
    private SelectionAdapter sa;
    private SimulationBtnHandler sbHandler;
    private TextField numTxt;
    private TextField simTxt;
    private TextField stepTxt;
    private TextField pathTxt;
    private JCheckBox outputSignal;
    private JTextArea textArea;
    private JComboBox genList;
    private SparkSession spark;

    public GenerateBtnHandler(
            SelectionAdapter sa,
            TextField numTxt,
            TextField simTxt,
            TextField stepTxt,
            TextField pathTxt,
            JCheckBox outputSignal,
            SimulationBtnHandler sbHandler,
            JTextArea textArea,
            JComboBox genList,
            SparkSession spark) {
        this.sa = sa;
        this.numTxt = numTxt;
        this.simTxt = simTxt;
        this.stepTxt = stepTxt;
        this.pathTxt = pathTxt;
        this.outputSignal = outputSignal;
        this.sbHandler = sbHandler;
        this.textArea = textArea;
        this.genList = genList;
        this.spark = spark;
    }

    private static Seq<Vehicle> convertListToSeq(List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }

    public void actionPerformed(ActionEvent e) {
        if (sa.getViewer().getOverlayPainter() == null
                || numTxt.getText() == null
                || numTxt.getText().length() == 0
                || simTxt.getText() == null
                || simTxt.getText().length() == 0
                || stepTxt.getText() == null
                || stepTxt.getText().length() == 0
                || pathTxt.getText() == null
                || pathTxt.getText().length() == 0) {
            AttentionDialog dialog =
                    new AttentionDialog(
                            "Attention",
                            "You must select an area, enter the required parameters first!");
        } else {
            Thread t =
                    new Thread(
                            () -> {
                                textArea.append("Processing...\n");
                                JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());

                                long startTime = System.currentTimeMillis();

                                final Rectangle2D[] bounds = sa.getPoints();
                                Point pt1 = new Point();
                                pt1.setLocation(bounds[0].getX(), bounds[0].getY());
                                Point pt2 = new Point();
                                pt2.setLocation(bounds[1].getX(), bounds[1].getY());

                                GeoPosition geo1 = sa.getViewer().convertPointToGeoPosition(pt1);
                                GeoPosition geo2 = sa.getViewer().convertPointToGeoPosition(pt2);

                                String type = genList.getSelectedIndex() == 0 ? "DSO" : "NB";
                                int total = Integer.parseInt(numTxt.getText());
                                int step = Integer.parseInt(simTxt.getText());
                                int timestep = Integer.parseInt(stepTxt.getText());
                                String outputPath = pathTxt.getText();

                                LOG.warn(
                                        String.format(
                                                "Selected rectangle, p1: %s, p2: %s", geo1, geo2));
                                textArea.append("Selected rectangle... \n");
                                textArea.append("p1: " + geo1 + "\n");
                                textArea.append("p2: " + geo2 + "\n");
                                textArea.append("Parameters... \n");
                                textArea.append(
                                        String.format(
                                                "Type: %s, Total: %s, Steps: %s, TimeStep: %s. \n",
                                                type, total, step, timestep));

                                Coordinate coor1 =
                                        new Coordinate(geo1.getLatitude(), geo1.getLongitude());
                                Coordinate coor2 =
                                        new Coordinate(geo2.getLatitude(), geo2.getLongitude());
                                Distance distance = new Distance();

                                double maxLen =
                                        distance.euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
                                Coordinate newCoor1 =
                                        new Coordinate(coor1.x + maxLen, coor1.y - maxLen);
                                Coordinate newCoor2 =
                                        new Coordinate(coor2.x + maxLen, coor2.y - maxLen);

                                textArea.append("Downloaded OSM file...\n");

                                HDFSUtil hdfs = new HDFSUtil(outputPath);
                                String name = "geosparksim";
                                hdfs.deleteDir(name);
                                hdfs.mkdir(name);
                                String output = outputPath + name;

                                OsmLoader osmLoader = new OsmLoader(newCoor1, newCoor2, output);
                                osmLoader.parquet();
                                osmLoader.osm();

                                textArea.append("Output Path: " + output + "\n");

                                RoadNetwork roadNetwork =
                                        OsmConverter.convertToRoadNetwork(spark, output);
                                textArea.append("Processing OSM...\n");
                                RoadNetworkWriter networkWriter =
                                        new RoadNetworkWriter(spark, roadNetwork, output);
                                networkWriter.writeEdgeJson();
                                textArea.append("Write edge into json. \n");
                                networkWriter.writeSignalJson();
                                textArea.append("Write signal into json. \n");
                                networkWriter.writeIntersectJson();
                                textArea.append("Write intersection into json. \n");
                                String osmPath = output + "/map.osm";
                                textArea.append("Generating vehicles...\n");
                                CreateVehicles createVehicles =
                                        new CreateVehicles(osmPath, coor1, coor2, maxLen);
                                List<Vehicle> vehicleList = null;
                                try {
                                    vehicleList = createVehicles.multiple(total, type);
                                } catch (InterruptedException | ExecutionException e1) {
                                    e1.printStackTrace();
                                }

                                VehicleHandler vehicleHandler = new VehicleHandler(spark, output);
                                vehicleHandler.writeVehicleTrajectoryJson(
                                        convertListToSeq(vehicleList));
                                textArea.append("Write vehicle into json. \n");
                                long endTime = System.currentTimeMillis();
                                textArea.append(
                                        "Finished preprocessing! Total time: "
                                                + (endTime - startTime) / 1000
                                                + " seconds\n");

                                textArea.append("Begin Simulation...\n");
                                RoadNetworkReader networkReader =
                                        new RoadNetworkReader(spark, output);
                                Dataset<Link> edges = networkReader.readEdgeJson();
                                Dataset<TrafficLight> signals = networkReader.readSignalJson();
                                Dataset<Intersect> intersects = networkReader.readIntersectJson();
                                Dataset<MOBILVehicle> vehicles =
                                        vehicleHandler.readVehicleTrajectoryJson();
                                textArea.append("Read edges, signals and vehicles...\n");

                                List<StepReport> res;
                                long simBegin = System.currentTimeMillis();

                                LOG.warn("Running in spark");
                                Microscopic.sim(
                                        spark,
                                        edges,
                                        signals,
                                        intersects,
                                        vehicles,
                                        output,
                                        step,
                                        timestep,
                                        total / 70,
                                        outputSignal.isSelected());
                                ReportHandler reportHandler = new ReportHandler(spark, output, 50);
                                Dataset<StepReport> reports = reportHandler.readReportJson();
                                res = reports.collectAsList();

                                //                    LocalSim temporalSim = new LocalSim();
                                //                    res = temporalSim.sim(edges, signals,
                                // vehicles, step, timestep);
                                //                    RDD<StepReport> resRDD =
                                // sc.parallelize(res).rdd();
                                //                    ReportHandler reportHandler = new
                                // ReportHandler(spark, output, 0);
                                //                    reportHandler.writeReportJson(resRDD, 0);

                                long simEnd = System.currentTimeMillis();
                                textArea.append(
                                        "Finished Simulation! Total time: "
                                                + (simEnd - simBegin) / 1000
                                                + " seconds\n");

                                textArea.append("Saved simulation reports into json file. \n");
                                sbHandler.setEdges(edges);
                                sbHandler.setReports(res);
                                textArea.append("You are ready for traffic visualization! \n");

                                double area = distance.rectArea(coor1.x, coor1.y, coor2.x, coor2.y);
                                if (total < 5000 && area < 15000000) {
                                    sbHandler.setUi(true);
                                } else {
                                    sbHandler.setUi(false);
                                    LOG.warn(
                                            "Because the number of vehicle is larger than 5000 or the area is larger than 15,000,000, "
                                                    + "GeoSparkSim will not show the traffic visualization! Please check output in "
                                                    + output);
                                }
                            });

            t.start();
        }
    }
}

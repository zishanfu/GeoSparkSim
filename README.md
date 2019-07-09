# <img src="https://github.com/zishanfu/GeoSparkSim/blob/dev/docs/images/GeoSparkSim.png" width="500">
[GeoSparkSim](http://www.public.asu.edu/~jiayu2/geospark/publication/geosparksim-mdm-2019.pdf) is a scalable microscopic traffic simulator, which extends Apache Spark to generate large-scale road network traffic data and help data scientists to simulate, analyze and visualize large-scale traffic data. GeoSparkSim converts road networks into Spark graphs, simulates vehicles to Vehicle Resilient Distributed Datasets (VehicleRDDs) and provides a simulation-aware vehicle paratitioning method to parallelize simulation steps, balance workload and handle the dynamic spatial distribution.

It is mainly developed by the contributors from [Data Systems Lab](https://www.datasyslab.net/)

### Installation and Get Start
* Download GeoSparkSim repository or `git clone https://github.com/zishanfu/GeoSparkSim.git`
* Run `cd GeoSparkSim`
* Run `mvn clean install` under GeoSparkSim folder

### Get Start
* Run command line `java -cp target/GeoSparkSim-1.0-SNAPSHOT-jar-with-dependencies.jar com.zishanfu.geosparksim.GeoSparkSim -h`
<img src="https://github.com/zishanfu/GeoSparkSim/blob/dev/docs/images/helper.png" align="center" width="700">

### Questions
Please contact ZishanFu@asu.edu


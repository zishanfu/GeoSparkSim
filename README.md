# <img src="https://github.com/zishanfu/GeoSparkSim/blob/dev/docs/images/GeoSparkSim.png" width="400">
[GeoSparkSim](http://www.public.asu.edu/~jiayu2/geospark/publication/geosparksim-mdm-2019.pdf) is a scalable microscopic traffic simulator, which extends Apache Spark to generate large-scale road network traffic data and help data scientists to simulate, analyze and visualize large-scale traffic data. GeoSparkSim converts road networks into Spark graphs, simulates vehicles to Vehicle Resilient Distributed Datasets (VehicleRDDs) and provides a simulation-aware vehicle partitioning method to parallelize simulation steps, balance workload and handle the dynamic spatial distribution.

It is mainly developed by the contributors from [Data Systems Lab](https://www.datasyslab.net/)

### Installation
* Download GeoSparkSim repository or run command

    `git clone https://github.com/zishanfu/GeoSparkSim.git`
* Run `cd GeoSparkSim`
* Run `mvn clean install` under GeoSparkSim folder

### Get Start
* Run command
`java -cp target/GeoSparkSim-1.0-SNAPSHOT-jar-with-dependencies.jar com.zishanfu.geosparksim.GeoSparkSim -h`
<p align="center"><img src="https://github.com/zishanfu/GeoSparkSim/blob/dev/docs/images/helper.png" width="500"></p>

* Run GeoSparkSim with `-o` to show the user interface
<p align="center"><img src="https://github.com/zishanfu/GeoSparkSim/blob/dev/docs/images/ui.png" width="500"></p>

* Run GeoSparkSim in standalone mode
```
  ./spark-folder/bin/spark-submit
  --class com.zishanfu.geosparksim.GeoSparkSim
  target/GeoSparkSim-1.0-SNAPSHOT-jar-with-dependencies.jar
```
* Run GeoSparkSim in distributed mode
```
  ./spark-folder/bin/spark-submit
  --master <master-url>
  --class com.zishanfu.geosparksim.GeoSparkSim
  target/GeoSparkSim-1.0-SNAPSHOT-jar-with-dependencies.jar
```
* More details from [GeoSparkSim Wiki](https://github.com/zishanfu/GeoSparkSim/wiki)

### References
* [Building a Large-Scale Microscopic Road Network Traffic Simulator in Apache Spark](https://ieeexplore.ieee.org/document/8788796)
Zishan Fu, Jia Yu, Mohamed Sarwat.
To appear in proceedings of the IEEE International Conference on Mobile Data Management, in Hong Kong, China June 2019.
* [Demonstrating GeoSparkSim: A Scalable Microscopic Road Network Traffic Simulator Based on Apache Spark](https://dl.acm.org/citation.cfm?id=3340984)
Zishan Fu, Jia Yu, and Mohamed Sarwat.
To appear in proceedings of the International Symposium on Spatial and SpatioTemporal Databases, in Vienna, Austria August 2019 (Demo Track).
<span style="color:red"> Best Demonstration Paper Award Runner-Up </span>
* [GeoSpark: A Cluster Computing Framework for Processing Large-Scale Spatial Data](https://dl.acm.org/citation.cfm?id=2820860)[[Project Website](https://datasystemslab.github.io/GeoSpark/)]
Jia Yu, Jinxuan Wu and Mohamed Sarwat.
In proceedings of the ACM International Conference on Advances in Geographic Information Systems, Seattle, WA, USA November 2015  (Short Paper)

### Questions
Please contact ZishanFu@asu.edu

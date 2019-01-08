package com.zishanfu.vistrips

class AppTestScala extends TestBaseScala {
  describe("VisTrips test") {
    it("Read CSV data") {
      var df = sparkSession.read.format("csv").option("delimiter", "\t").option("header", "false").load(csvData)
      df.show()
    }
  }
}

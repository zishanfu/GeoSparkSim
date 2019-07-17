package com.zishanfu.geosparksim.tools

import java.net.URI

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

class HDFSUtil (hdfsUrl: String){
  var targetUrl = ""
  val config = new Configuration()
  config.setBoolean("fs.hdfs.impl.disable.cache", true)
//  if(distributed){
//    config.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
//  }

  def getHDFSUrl() : String = hdfsUrl


  def mkdir(dir : String) : Boolean = {
    var result = false
    if (StringUtils.isNoneBlank(dir)) {
      targetUrl = hdfsUrl + dir

      val fs = FileSystem.get(URI.create(targetUrl), config)
      if (!fs.exists(new Path(targetUrl))) {
        fs.mkdirs(new Path(targetUrl))
      }
      fs.close()
      result = true
    }
    result
  }

  def deleteDir(dir : String) : Boolean = {
    var result = false
    if (StringUtils.isNoneBlank(dir)) {
      targetUrl = hdfsUrl + dir
      val fs = FileSystem.get(URI.create(targetUrl), config)
      fs.delete(new Path(targetUrl), true)
      fs.close()
      result = true
    }
    result
  }


  def uploadLocalFile2HDFS(localFile : String, hdfsFile : String) : String = {
    if (StringUtils.isNoneBlank(localFile) && StringUtils.isNoneBlank(hdfsFile)) {
      targetUrl = hdfsUrl + hdfsFile
      val hdfs = FileSystem.get(URI.create(hdfsUrl), config)
      val src = new Path(localFile)
      val dst = new Path(targetUrl)
      hdfs.copyFromLocalFile(src, dst)
      hdfs.close()
      targetUrl
    }
    ""
  }

  def createNewHDFSFile(newFile : String, content : String) : Boolean = {
    var result = false
    if (StringUtils.isNoneBlank(newFile) && null != content) {
      targetUrl = hdfsUrl + newFile
      val hdfs = FileSystem.get(URI.create(targetUrl), config)
      val os = hdfs.create(new Path(targetUrl))
      os.write(content.getBytes("UTF-8"))
      os.close()
      hdfs.close()
      result = true
    }
    result
  }

  def deleteHDFSFile(hdfsFile : String) : Boolean = {
    var result = false
    if (StringUtils.isNoneBlank(hdfsFile)) {
      targetUrl = hdfsUrl + hdfsFile
      val hdfs = FileSystem.get(URI.create(targetUrl), config)
      val path = new Path(targetUrl)
      val isDeleted = hdfs.delete(path, true)
      hdfs.close()
      result = isDeleted
    }
    result
  }

  def readHDFSFile(hdfsFile : String) : Array[Byte] = {
    var result =  new Array[Byte](0)
    if (StringUtils.isNoneBlank(hdfsFile)) {
      targetUrl = hdfsUrl + hdfsFile
      val hdfs = FileSystem.get(URI.create(targetUrl), config)
      val path = new Path(targetUrl)
      if (hdfs.exists(path)) {
        val inputStream = hdfs.open(path)
        val stat = hdfs.getFileStatus(path)
        val length = stat.getLen.toInt
        val buffer = new Array[Byte](length)
        inputStream.readFully(buffer)
        inputStream.close()
        hdfs.close()
        result = buffer
      }
    }
    result
  }

}

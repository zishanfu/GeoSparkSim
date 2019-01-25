package com.zishanfu.vistrips.osm;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.parquet.hadoop.ParquetWriter;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import com.zishanfu.vistrips.osm.parquet.ParquetWriterFactory;


public class OsmParquetSink implements Sink {
	
    private String destinationFolder;

    private ParquetWriter<Node> nodeWriter;
    private ParquetWriter<Way> waysWriter;

    public OsmParquetSink(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    @Override
    public void initialize(Map<String, Object> metaData) {
    	final String destNode = destinationFolder + "/" + format("%s.parquet", "node");
        final String destWays = destinationFolder + "/" + format("%s.parquet", "way");
    	try {
        	this.nodeWriter = ParquetWriterFactory.buildFor(destNode,EntityType.Node);
        	this.waysWriter = ParquetWriterFactory.buildFor(destWays,EntityType.Way);
        } catch (IOException e) {
            throw new RuntimeException("Unable to build writers", e);
        }
    }

    @Override
    public void process(EntityContainer entityContainer) {
    	Entity entity = entityContainer.getEntity();
    	
    	try {
        	if(entity instanceof Node) {
        		nodeWriter.write((Node) entity);
        	}else if(entity instanceof Way) {
        		Set<String> tagSet = entity.getTags().stream().map(Tag::getKey).collect(Collectors.toSet());
    			if(tagSet.contains("highway")) {
    				waysWriter.write((Way) entity);
    	    	}
        	}
    	}catch(IOException e){
    		throw new RuntimeException("Unable to write osm parquet", e);
    	}
    }
    

    @Override
    public void complete() {
        try {
        	nodeWriter.close();
        	waysWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close writers", e);
        }
    }

    @Override
    public void release() {

    }

}


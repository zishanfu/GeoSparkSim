package com.zishanfu.geosparksim.OSM;

import static java.lang.String.format;

import java.io.IOException;
import java.util.*;

import com.zishanfu.geosparksim.OSM.parquet.ParquetWriterFactory;
import org.apache.parquet.hadoop.ParquetWriter;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


public class OsmParquetSink implements Sink {

    private String destinationFolder;

    private ParquetWriter<Node> nodeWriter;
    private ParquetWriter<Way> waysWriter;
    //filter footway, cycleway, bus way, etc

    private Set<String> set = new HashSet<>(Arrays.asList(
            "motorway",
            "motorway_link",
            "trunk",
            "trunk_link",
            "primary",
            "primary_link",
            "secondary",
            "secondary_link",
            "tertiary",
            "tertiary_link",
            "living_street",
            "residential",
            "road",
            "construction",
            "unclassified",
            "track",
            "motorway_junction",
            "service"));

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
                if(tagsCheck(entity.getTags())) {
                    waysWriter.write((Way) entity);
                }
            }
        }catch(IOException e){
            throw new RuntimeException("Unable to write osm parquet", e);
        }
    }

    private boolean tagsCheck(Collection<Tag> tags){
        boolean result = false;
        for(Tag tag: tags){
            if(tag.getKey().equals("highway")){
                result = true;
                String value = tag.getValue();
                if(!set.contains(value)){
                    return false;
                }
            }
        }
        return result;
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

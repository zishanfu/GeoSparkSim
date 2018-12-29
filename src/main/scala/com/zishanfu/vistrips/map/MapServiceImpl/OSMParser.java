package com.zishanfu.vistrips.map.MapServiceImpl;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

public class OSMParser {
	public OSMParser(String osm) {
		// /Users/SammiFu/Downloads/mapsource/AZ/arizona-latest.osm.pbf
		File osmFile = new File(osm);
        PbfReader reader = new PbfReader(osmFile, 1);
 
        final AtomicInteger numberOfNodes = new AtomicInteger();
        final AtomicInteger numberOfWays = new AtomicInteger();
        final AtomicInteger numberOfRelations = new AtomicInteger();
 
        Sink sinkImplementation = new Sink() {
 
            public void process(EntityContainer entityContainer) {
 
                Entity entity = entityContainer.getEntity();
                
                if (entity instanceof Node) {
                		//node[id, tags]
                	Iterator<Tag> it = entity.getTags().iterator();
            		while(it.hasNext()) {
            			Tag i = it.next();
            			if(i.getKey().contains("restriction")) {
            				System.out.println(i.toString());
            			}
            		}
                    numberOfNodes.incrementAndGet();
                } else if (entity instanceof Way) {
                		//Way[id, tags, name]
         
                    numberOfWays.incrementAndGet();
                } else if (entity instanceof Relation) {
                		//Relation[id, tags, type]
                    numberOfRelations.incrementAndGet();
                }
            }
 
            public void initialize(Map<String, Object> arg0) {
            }
 
            public void complete() {
            }
 
            public void release() {
            }
 
        };
 
        reader.setSink(sinkImplementation);
        reader.run();
 
        System.out.println(numberOfNodes.get() + " Nodes are found.");
        System.out.println(numberOfWays.get() + " Ways are found.");
        System.out.println(numberOfRelations.get() + " Relations are found.");
	}
}

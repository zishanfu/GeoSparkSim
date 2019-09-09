package com.zishanfu.geosparksim.osm.parquet;


import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.DOUBLE;
import static org.apache.parquet.schema.Type.Repetition.REQUIRED;

import java.util.ArrayList;
import java.util.List;

import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;


public class NodeWriteSupport extends OsmEntityWriteSupport<Node> {

    private final PrimitiveType latType;
    private final PrimitiveType longType;

    public NodeWriteSupport() {
        latType = new PrimitiveType(REQUIRED, DOUBLE, "latitude");
        longType = new PrimitiveType(REQUIRED, DOUBLE, "longitude");
    }

    @Override
    protected MessageType getSchema() {
        final List<Type> attributes = new ArrayList<>(getCommonAttributes());
        attributes.add(latType);
        attributes.add(longType);
        return new MessageType("node", attributes);
    }

    @Override
    protected void writeSpecificFields(Node record, int nextAvailableIndex) {
        recordConsumer.startField(latType.getName(), nextAvailableIndex);
        recordConsumer.addDouble(record.getLatitude());
        recordConsumer.endField(latType.getName(), nextAvailableIndex++);

        recordConsumer.startField(longType.getName(), nextAvailableIndex);
        recordConsumer.addDouble(record.getLongitude());
        recordConsumer.endField(longType.getName(), nextAvailableIndex);
    }
}
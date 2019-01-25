package com.zishanfu.vistrips.osm.parquet;

import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.BINARY;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT64;
import static org.apache.parquet.schema.Type.Repetition.OPTIONAL;
import static org.apache.parquet.schema.Type.Repetition.REPEATED;
import static org.apache.parquet.schema.Type.Repetition.REQUIRED;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

public abstract class OsmEntityWriteSupport<E extends Entity> extends WriteSupport<E> {

    private final PrimitiveType idType;
    private final GroupType tags;
    private final PrimitiveType tagKeyType;
    private final PrimitiveType tagValueType;

    protected RecordConsumer recordConsumer;

    public OsmEntityWriteSupport() {
        idType = new PrimitiveType(REQUIRED, INT64, "id");
        tagKeyType = new PrimitiveType(REQUIRED, BINARY, "key");
        tagValueType = new PrimitiveType(OPTIONAL, BINARY, "value");
        tags = new GroupType(REPEATED, "tags", tagKeyType, tagValueType);
    }

    protected List<Type> getCommonAttributes() {
        final List<Type> commonAttributes = new LinkedList<>();
        commonAttributes.add(idType);
        commonAttributes.add(tags);
        return commonAttributes;
    }

    @Override
    public WriteContext init(Configuration config) {
        return new WriteContext(getSchema(), Collections.emptyMap());
    }

    protected abstract MessageType getSchema();

    @Override
    public void prepareForWrite(RecordConsumer recordConsumer) {
        this.recordConsumer = recordConsumer;
    }

    protected abstract void writeSpecificFields(E record, int nextAvailableIndex);

    public void write(E record) {
        int index = 0;
        recordConsumer.startMessage();
        recordConsumer.startField(idType.getName(), index);
        recordConsumer.addLong(record.getId());
        recordConsumer.endField(idType.getName(), index++);

        if (!record.getTags().isEmpty()) {
            recordConsumer.startField(tags.getName(), index);
            for (Tag tag : record.getTags()) {
                recordConsumer.startGroup();

                recordConsumer.startField(tagKeyType.getName(), 0);
                recordConsumer.addBinary(Binary.fromString(tag.getKey()));
                recordConsumer.endField(tagKeyType.getName(), 0);

                recordConsumer.startField(tagValueType.getName(), 1);
                recordConsumer.addBinary(Binary.fromString(tag.getValue()));
                recordConsumer.endField(tagValueType.getName(), 1);

                recordConsumer.endGroup();
            }
            recordConsumer.endField(tags.getName(), index);
        }
        index++;

        writeSpecificFields(record, index);
        recordConsumer.endMessage();
    }
}

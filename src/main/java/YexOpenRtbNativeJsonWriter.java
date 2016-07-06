/**
 * @(#)YexOpenRtbNativeJsonWriter, 16/6/15.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbNativeJsonWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YexOpenRtbNativeJsonWriter extends OpenRtbNativeJsonWriter {

    protected YexOpenRtbNativeJsonWriter(OpenRtbJsonFactory factory) {
        super(factory);
    }

    /**
     * Serializes a {@link OpenRtb.NativeRequest} to JSON, with a provided {@link JsonGenerator}
     * which allows several choices of output and encoding.
     */
    public final void yexWriteNativeRequest(OpenRtb.NativeRequest req, JsonGenerator gen) throws IOException {
        // gen.writeStartObject();
        // gen.writeObjectFieldStart("native");
        writeNativeRequestFields(req, gen);
        writeExtensions(req, gen);
        //gen.writeEndObject();
        //gen.writeEndObject();
    }

}

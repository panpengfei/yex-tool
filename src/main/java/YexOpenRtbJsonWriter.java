/**
 * @(#)YexOpenRtbJsonWriter, 16/6/15.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonWriter;

import java.io.IOException;

import static com.google.openrtb.json.OpenRtbJsonUtils.writeEnums;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YexOpenRtbJsonWriter extends OpenRtbJsonWriter {

    private YexOpenRtbNativeJsonWriter yexNativeWriter;

    protected YexOpenRtbJsonWriter(YexOpenRtbJsonFactory factory) {
        super(factory);
    }

    protected final YexOpenRtbNativeJsonWriter yexNativeWriter() {
        if (yexNativeWriter == null) {
            yexNativeWriter = ((YexOpenRtbJsonFactory) factory()).newNativeWriter();
        }
        return yexNativeWriter;
    }


    @Override
    protected void writeNativeFields(OpenRtb.BidRequest.Imp.Native nativ, JsonGenerator gen) throws IOException {
        switch (nativ.getRequestOneofCase()) {
            case REQUEST_NATIVE:
                //gen.writeFieldName("request");
                yexNativeWriter().yexWriteNativeRequest(nativ.getRequestNative(), gen);
                break;
            case REQUEST:
                gen.writeStringField("request", nativ.getRequest());
                break;
            case REQUESTONEOF_NOT_SET:
                checkRequired(false);
        }
        if (nativ.hasVer()) {
            gen.writeStringField("ver", nativ.getVer());
        }
        writeEnums("api", nativ.getApiList(), gen);
        writeEnums("battr", nativ.getBattrList(), gen);
    }

}

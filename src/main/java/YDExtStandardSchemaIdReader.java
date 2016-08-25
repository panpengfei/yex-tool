/**
 * @(#)YDExtStandardSchemaIdReader, 16/8/18.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonExtReader;
import com.google.openrtb.youdao.OpenRtbYDExtForDsp;

import java.io.IOException;

import static com.google.openrtb.json.OpenRtbJsonUtils.getCurrentName;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YDExtStandardSchemaIdReader extends OpenRtbJsonExtReader<OpenRtb.BidRequest.Imp.Builder> {

    public YDExtStandardSchemaIdReader() {
        super(Constants.EXTEND_STANDARD_SCHEMA_ID_FIELD_NAME);
    }


    @Override
    protected void read(OpenRtb.BidRequest.Imp.Builder message, JsonParser par) throws IOException {
        if (Constants.EXTEND_STANDARD_SCHEMA_ID_FIELD_NAME.equals(getCurrentName(par))) {
            message.setExtension(OpenRtbYDExtForDsp.ssid, par.nextIntValue(-1));
        }
    }

}

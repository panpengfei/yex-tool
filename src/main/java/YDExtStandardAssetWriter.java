/**
 * @(#)YDExtStandardAssetWriter, 16/8/11.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonExtWriter;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbNativeJsonWriter;

import java.io.IOException;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YDExtStandardAssetWriter extends OpenRtbJsonExtWriter<OpenRtb.NativeRequest.Asset> {

    private static OpenRtbNativeJsonWriter openRtbNativeJsonWriter = OpenRtbJsonFactory.create()
            .register(new YDExtDataAssetTypeWriter(), Integer.class, OpenRtb.NativeRequest.Asset.Data.class, Constants.EXTEND_DATA_ASSET_TYPE_FIELD_NAME)
            .newNativeWriter();

    @Override
    protected void write(OpenRtb.NativeRequest.Asset ext, JsonGenerator gen) throws IOException {
        gen.writeFieldName(Constants.EXTEND_STANDARD_ASSET_FIELD_NAME);
        openRtbNativeJsonWriter.writeReqAsset(ext, gen);
    }
}

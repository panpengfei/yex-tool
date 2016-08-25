/**
 * @(#)YDExtStandardAssetReader, 16/8/18.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonExtComplexReader;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbNativeJsonReader;
import com.google.protobuf.GeneratedMessage;

import java.io.IOException;

import static com.google.openrtb.json.OpenRtbJsonUtils.getCurrentName;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YDExtStandardAssetReader<EB extends GeneratedMessage.ExtendableBuilder<?, EB>>
        extends OpenRtbJsonExtComplexReader<EB, OpenRtb.NativeRequest.Asset.Builder> {


    public YDExtStandardAssetReader(GeneratedMessage.GeneratedExtension<?, ?> key, String name) {
        super(key, true, name);
    }

    private static OpenRtbNativeJsonReader openRtbNativeJsonReader = OpenRtbJsonFactory.create()
            .register(new YDExtDataAssetTypeReader(), OpenRtb.NativeRequest.Asset.Data.Builder.class)
            .newNativeReader();


    @Override
    protected void read(OpenRtb.NativeRequest.Asset.Builder ext, JsonParser par) throws IOException {
        switch (getCurrentName(par)) {
            case "id":
                ext.setId(par.nextIntValue(-1));
                break;
            case "required":
                ext.setRequired(par.nextIntValue(0) != 0);
                break;
            case "title":
                ext.setTitle(openRtbNativeJsonReader.readReqTitle(par));
                break;
            case "img":
                ext.setImg(openRtbNativeJsonReader.readReqImage(par));
                break;
            case "data":
                ext.setData(openRtbNativeJsonReader.readReqData(par));
                break;
        }
    }
}

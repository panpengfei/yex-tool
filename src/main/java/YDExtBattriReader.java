/**
 * @(#)YDExtBattriReader, 16/2/22.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonExtReader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.openrtb.json.OpenRtbJsonUtils.*;

import com.google.openrtb.youdao.*;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YDExtBattriReader extends OpenRtbJsonExtReader<OpenRtb.BidRequest.Imp.Native.Builder> {
    private static Logger logger = Logger.getLogger(YDExtBattriReader.class.getName());
    private static final String BATTRI_FIELD_NAME = "battri";

    @Override
    protected void read(OpenRtb.BidRequest.Imp.Native.Builder message, JsonParser par) throws IOException {
        if (BATTRI_FIELD_NAME.equals(getCurrentName(par))) {
            List<Integer> battris = new ArrayList<Integer>();
            for (startArray(par); endArray(par); par.nextToken()) {
                try {
                    int battri = Integer.parseInt(par.getText());
                    battris.add(battri);
                } catch (Exception e) {
                    logger.warn("battri is not a int value.", e);
                }
            }
            if (battris.isEmpty()) return;
            message.setExtension(OpenRtbYDExtForDsp.battri, battris);
        }
    }
}

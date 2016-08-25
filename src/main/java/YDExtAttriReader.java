/**
 * @(#)YDExtAttriReader, 16/6/17.
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
public class YDExtAttriReader extends OpenRtbJsonExtReader<OpenRtb.BidResponse.SeatBid.Bid.Builder> {
    private static Logger logger = Logger.getLogger(YDExtAttriReader.class.getName());

    @Override
    protected void read(OpenRtb.BidResponse.SeatBid.Bid.Builder message, JsonParser par) throws IOException {
        if (Constants.EXTEND_ATTRI_FIELD_NAME.equals(getCurrentName(par))) {
            List<Integer> attris = new ArrayList<>();
            for (startArray(par); endArray(par); par.nextToken())
                try {
                    int battri = Integer.parseInt(par.getText());
                    attris.add(battri);
                } catch (Exception e) {
                    logger.warn("attri is not a int value.", e);
                }
            if (attris.isEmpty()) return;
            message.setExtension(OpenRtbYDExtForDsp.attri, attris);
        }
    }
}

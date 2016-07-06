/**
 * @(#)YexOpenRtbJsonReader, 16/6/17.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.google.openrtb.util.OpenRtbUtils;

import java.io.IOException;

import static com.google.openrtb.json.OpenRtbJsonUtils.*;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YexOpenRtbJsonReader extends OpenRtbJsonReader {

    protected YexOpenRtbJsonReader(YexOpenRtbJsonFactory factory) {
        super(factory);
    }

    @Override
    protected void readBidField(JsonParser par, OpenRtb.BidResponse.SeatBid.Bid.Builder bid, String fieldName)
            throws IOException {
        switch (fieldName) {
            case "id":
                bid.setId(par.getText());
                break;
            case "impid":
                bid.setImpid(par.getText());
                break;
            case "price":
                bid.setPrice(getDoubleValue(par));
                break;
            case "adid":
                bid.setAdid(par.getText());
                break;
            case "nurl":
                bid.setNurl(par.getText());
                break;
            case "adm":
                bid.setAdmNative(factory().newNativeReader().readNativeResponse(par));
                break;
            case "adomain":
                for (startArray(par); endArray(par); par.nextToken()) {
                    bid.addAdomain(par.getText());
                }
                break;
            case "bundle":
                bid.setBundle(par.getText());
                break;
            case "iurl":
                bid.setIurl(par.getText());
                break;
            case "cid":
                bid.setCid(par.getText());
                break;
            case "crid":
                bid.setCrid(par.getText());
                break;
            case "cat":
                for (startArray(par); endArray(par); par.nextToken()) {
                    String cat = par.getText();
                    if (OpenRtbUtils.categoryFromName(cat) != null) {
                        bid.addCat(cat);
                    }
                }
                break;
            case "attr":
                for (startArray(par); endArray(par); par.nextToken()) {
                    OpenRtb.CreativeAttribute value = OpenRtb.CreativeAttribute.valueOf(par.getIntValue());
                    if (checkEnum(value)) {
                        bid.addAttr(value);
                    }
                }
                break;
            case "dealid":
                bid.setDealid(par.getText());
                break;
            case "w":
                bid.setW(par.getIntValue());
                break;
            case "h":
                bid.setH(par.getIntValue());
                break;
            default:
                readOther(bid, par, fieldName);
        }
    }
}

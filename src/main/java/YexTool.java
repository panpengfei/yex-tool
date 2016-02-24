/**
 * @(#)YexTool, 16/2/21.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.google.protobuf.ExtensionRegistry;
import org.apache.commons.cli.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import com.google.openrtb.youdao.*;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YexTool {

    private static Logger logger = Logger.getLogger(YexTool.class.getName());
    private static Option helpOption = new Option("h", "help", false,
            "display help message");
    private static Option serverAddressOption = Option.builder("s")
            .hasArgs()
            .argName("server address")
            .desc("server address")
            .required()
            .build();
    private static Option inputFileOption = Option.builder("f")
            .hasArgs()
            .argName("input file")
            .desc("input file")
            .required()
            .build();
    private static final Options options = new Options()
            .addOption(helpOption)
            .addOption(inputFileOption)
            .addOption(serverAddressOption);
    private static ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();

    static {
        OpenRtbYDExtForDsp.registerAllExtensions(extensionRegistry);
    }

    private static OpenRtb.BidRequest buildBidRequestFromFile(String path) throws IOException {
        Reader fileReader = new FileReader(path);
        OpenRtbJsonFactory openRtbJsonFactory = OpenRtbJsonFactory.create()
                .register(new YDExtReader(), OpenRtb.BidRequest.Imp.Native.Builder.class);
        OpenRtbJsonReader reader = openRtbJsonFactory.newReader();
        return reader.readBidRequest(fileReader);
    }


    private static OpenRtb.BidResponse sendBidRequest(String dspServerAddress, OpenRtb.BidRequest bidRequest) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(dspServerAddress);

        ByteArrayEntity entity = new ByteArrayEntity(bidRequest.toByteArray());
        post.setEntity(entity);

        logger.info("Sending BidRequest to URL: " + dspServerAddress);
        HttpResponse response = httpclient.execute(post);

        OpenRtb.BidResponse bidResponse = null;
        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                bidResponse = OpenRtb.BidResponse.parseFrom(response.getEntity().getContent(), extensionRegistry);
            } catch (Exception e) {
                throw new Exception("Error while parse HttpResponse to BidResponse!", e);
            }
            bidResponse = electValidBidsInBidResponse(bidRequest, bidResponse);
        } else {
            logger.error("Error Response Code: " + response.getStatusLine().getStatusCode());
        }

        return bidResponse;
    }

    /**
     * Elect out valid bids
     * 期间去掉了不合法bid,不合法的case包括:
     * a. impId不包含在bidRequest里
     * b. RequestNative里要求的assetIds没有完全满足
     * c. 价格低于bidFloor
     * d. 未指明广告的attri,或者attri被block
     * e. 未指明广告是下载型广告还是落地页型
     */
    private static OpenRtb.BidResponse electValidBidsInBidResponse(OpenRtb.BidRequest bidRequest, OpenRtb.BidResponse bidResponse) {
        List<String> impIds = new ArrayList<String>();
        Map<String, Double> impId2Bidfloors = new HashMap<String, Double>();
        Map<String, List<Integer>> impId2AssetIds = new HashMap<String, List<Integer>>();
        Map<String, List<Integer>> impId2Battributes = new HashMap<String, List<Integer>>();
        for (OpenRtb.BidRequest.Imp imp : bidRequest.getImpList()) {
            impIds.add(imp.getId());
            impId2Bidfloors.put(imp.getId(), imp.getBidfloor());
            impId2Battributes.put(imp.getId(), imp.getNative().getExtension(OpenRtbYDExtForDsp.battri));

            List<Integer> assetIds = new ArrayList<Integer>();
            for (OpenRtb.NativeRequest.Asset asset : imp.getNative().getRequestNative().getAssetsList()) {
                assetIds.add(asset.getId());
            }
            impId2AssetIds.put(imp.getId(), assetIds);
        }

        OpenRtb.BidResponse.Builder bidResponseBuilder = bidResponse.toBuilder().clearSeatbid();
        for (OpenRtb.BidResponse.SeatBid seatBid : bidResponse.getSeatbidList()) {
            OpenRtb.BidResponse.SeatBid.Builder validSeatBidBuilder = seatBid.toBuilder().clearBid();
            for (OpenRtb.BidResponse.SeatBid.Bid bid : seatBid.getBidList()) {
                if (isBidValid(bid, impIds, impId2Bidfloors, impId2AssetIds, impId2Battributes)) {
                    validSeatBidBuilder.addBid(bid.toBuilder());
                }
            }

            bidResponseBuilder.addSeatbid(validSeatBidBuilder);
        }

        return bidResponseBuilder.build();
    }


    private static boolean isBidValid(OpenRtb.BidResponse.SeatBid.Bid bid,
                                      List<String> impIds,
                                      Map<String, Double> impId2Bidfloors,
                                      Map<String, List<Integer>> impId2AssetIds,
                                      Map<String, List<Integer>> impId2Battributes) {
        return isValidImpId(bid, impIds) && isValidPrice(bid, impId2Bidfloors) && isValidAssets(bid, impId2AssetIds) && isValidAttrs(bid, impId2Battributes);
    }

    private static boolean isValidImpId(OpenRtb.BidResponse.SeatBid.Bid bid, List<String> impIds) {
        boolean valid = impIds.contains(bid.getImpid());
        if (!valid) {
            logger.warn("Imp id is invalid! Bid: \n" + bid);
        }
        return valid;
    }

    private static boolean isValidPrice(OpenRtb.BidResponse.SeatBid.Bid bid, Map<String, Double> impId2Bidfloors) {
        boolean valid = impId2Bidfloors.get(bid.getImpid()) <= bid.getPrice();
        if (!valid) {
            logger.warn("Price is under floor! Bid: \n" + bid);
        }
        return valid;
    }

    private static boolean isValidAssets(OpenRtb.BidResponse.SeatBid.Bid bid, Map<String, List<Integer>> impId2AssetIds) {
        List<Integer> requestAssetIds = impId2AssetIds.get(bid.getImpid());
        boolean valid = false;
        if (requestAssetIds != null) {
            List<Integer> bidAssetIds = new ArrayList<Integer>();
            for (OpenRtb.NativeResponse.Asset asset : bid.getAdmNative().getAssetsList()) {
                bidAssetIds.add(asset.getId());
            }
            Collections.sort(requestAssetIds);
            Collections.sort(bidAssetIds);

            valid = requestAssetIds.equals(bidAssetIds);
        }
        if (!valid) {
            logger.warn("Asset ids are invalid! Bid: \n" + bid);
        }
        return valid;
    }

    private static boolean isValidAttrs(OpenRtb.BidResponse.SeatBid.Bid bid, Map<String, List<Integer>> impId2Battributes) {
        List<Integer> battris = impId2Battributes.get(bid.getImpid());
        List<Integer> attris = new ArrayList<Integer>(bid.getExtension(OpenRtbYDExtForDsp.attri));
        boolean attrisIsEmpty = attris.isEmpty();
        attris.retainAll(battris);
        boolean attrisNotBlocked = attris.isEmpty();

        boolean valid = !attrisIsEmpty && attrisNotBlocked;

        if (attrisIsEmpty) {
            logger.warn("Attri is empty! Bid: \n" + bid);
        } else if (!attrisNotBlocked) {
            logger.warn("Attri is blocked! Bid: \n" + bid);
        }
        return valid;
    }

    private static void handleCommand(CommandLine commandLine) throws Exception {
        String requestFile = commandLine.getOptionValue(inputFileOption.getOpt());
        logger.info("Parsing bid request json file: " + requestFile);

        OpenRtb.BidRequest bidRequest = buildBidRequestFromFile(requestFile);
        logger.info("Bid request: \n" + bidRequest);

        String bidServer = commandLine.getOptionValue(serverAddressOption.getOpt());
        logger.info("Using bid server: " + bidServer);

        OpenRtb.BidResponse bidResponse = sendBidRequest(bidServer, bidRequest);
        logger.info("Bid response: \n" + bidResponse);
    }


    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar target/yex-tool-1.0.jar ", "Tool to test Yex integration \n",
                options, "", true);
    }


    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp();
            System.exit(1);
        }

        //check help option
        if (commandLine.hasOption(helpOption.getOpt())) {
            printHelp();
            System.exit(0);
        }

        // handle command
        try {
            handleCommand(commandLine);
            System.exit(0);
        } catch (Exception e) {
            logger.error(e);
            System.exit(1);
        }
    }
}

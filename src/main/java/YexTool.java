/**
 * @(#)YexTool, 16/2/21.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.google.openrtb.youdao.*;
import com.google.protobuf.ExtensionRegistry;
import org.apache.commons.cli.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YexTool {

    private static Logger logger = Logger.getLogger(YexTool.class.getName());
    private static final String BATTRI_FIELD_NAME = "battri";
    private static final String DATA_ASSET_TYPE_FIELD_NAME = "dataAssetType";


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
    private static OptionGroup dataFormatOptionGroup = new OptionGroup()
            .addOption(Option.builder("pb")
                    .hasArg(false)
                    .desc("protocol buffers format, as default value")
                    .build())
            .addOption(Option.builder("js")
                    .hasArg(false)
                    .desc("json format, and NativeRequest/NativeResponse json string format")
                    .build())
            .addOption(Option.builder("jo")
                    .hasArg(false)
                    .desc("json format, and NativeRequest/NativeResponse json object format")
                    .build());
    private static final Options options = new Options()
            .addOption(helpOption)
            .addOption(inputFileOption)
            .addOption(serverAddressOption)
            .addOptionGroup(dataFormatOptionGroup);
    private static ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();

    static {
        OpenRtbYDExtForDsp.registerAllExtensions(extensionRegistry);
    }

    private static OpenRtbJsonFactory openRtbJsonFactory = OpenRtbJsonFactory.create()
            .register(new YDExtBattriReader(), OpenRtb.BidRequest.Imp.Native.Builder.class)
            .register(new YDExtBattriWriter(), Integer.class, OpenRtb.BidRequest.Imp.Native.class, BATTRI_FIELD_NAME)
            .register(new YDExtDataAssetTypeWriter(), Integer.class, OpenRtb.NativeRequest.Asset.Data.class, DATA_ASSET_TYPE_FIELD_NAME)
            .register(new YDExtAttriReader(), OpenRtb.BidResponse.SeatBid.Bid.Builder.class);

    private static YexOpenRtbJsonFactory yexOpenRtbJsonFactory = YexOpenRtbJsonFactory.create()
            .yexRegister(new YDExtBattriReader(), OpenRtb.BidRequest.Imp.Native.Builder.class)
            .yexRegister(new YDExtBattriWriter(), Integer.class, OpenRtb.BidRequest.Imp.Native.class, BATTRI_FIELD_NAME)
            .yexRegister(new YDExtDataAssetTypeWriter(), Integer.class, OpenRtb.NativeRequest.Asset.Data.class, DATA_ASSET_TYPE_FIELD_NAME)
            .yexRegister(new YDExtAttriReader(), OpenRtb.BidResponse.SeatBid.Bid.Builder.class);

    private static OpenRtb.BidRequest buildBidRequestFromFile(String path) throws IOException {
        Reader fileReader = new FileReader(path);
        OpenRtbJsonReader reader = openRtbJsonFactory.newReader();
        return reader.readBidRequest(fileReader);
    }


    private static OpenRtb.BidResponse sendBidRequest(String dspServerAddress, OpenRtb.BidRequest bidRequest, String dataFormat) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(dspServerAddress);
        switch (dataFormat) {
            case "pb":
                post.setEntity(new ByteArrayEntity(bidRequest.toByteArray()));
                post.setHeader("Content-Type", "application/x-protobuf");
                break;
            case "js":
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                openRtbJsonFactory.newWriter().writeBidRequest(bidRequest, os);
                post.setEntity(new ByteArrayEntity(os.toByteArray()));
                post.setHeader("Content-Type", "application/json");
                break;
            case "jo":
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                yexOpenRtbJsonFactory.newWriter().writeBidRequest(bidRequest, os);
                post.setEntity(new ByteArrayEntity(os.toByteArray()));
                post.setHeader("Content-Type", "application/json");
                break;
        }

        logger.info("Sending BidRequest to URL: " + dspServerAddress);
        HttpResponse response = httpclient.execute(post);

        OpenRtb.BidResponse bidResponse = null;
        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                switch (dataFormat) {
                    case "pb":
                        bidResponse = OpenRtb.BidResponse.parseFrom(response.getEntity().getContent(), extensionRegistry);
                        break;
                    case "js":
                        bidResponse = openRtbJsonFactory.newReader().readBidResponse(response.getEntity().getContent());
                        OpenRtb.BidResponse.Builder bidResponseBuilder = bidResponse.toBuilder().clearSeatbid();
                        for (OpenRtb.BidResponse.SeatBid seatBid : bidResponse.getSeatbidList()) {
                            OpenRtb.BidResponse.SeatBid.Builder seatBidBuilder = seatBid.toBuilder().clearBid();
                            for (OpenRtb.BidResponse.SeatBid.Bid bid : seatBid.getBidList()) {
                                seatBidBuilder.addBid(
                                        bid.toBuilder()
                                                .clearAdm()
                                                .setAdmNative(openRtbJsonFactory.newNativeReader().readNativeResponse(bid.getAdm())));
                            }
                            bidResponseBuilder.addSeatbid(seatBidBuilder);
                        }
                        bidResponse = bidResponseBuilder.build();
                        break;
                    case "jo":
                        bidResponse = yexOpenRtbJsonFactory.newReader().readBidResponse(response.getEntity().getContent());
                        break;
                }
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
        Map<String, OpenRtb.BidRequest.Imp> impId2Imps = new HashMap<String, OpenRtb.BidRequest.Imp>();
        Map<String, Double> impId2Bidfloors = new HashMap<String, Double>();
        Map<String, List<Integer>> impId2Battributes = new HashMap<String, List<Integer>>();
        for (OpenRtb.BidRequest.Imp imp : bidRequest.getImpList()) {
            impIds.add(imp.getId());
            impId2Imps.put(imp.getId(), imp);
            impId2Bidfloors.put(imp.getId(), imp.getBidfloor());
            impId2Battributes.put(imp.getId(), imp.getNative().getExtension(OpenRtbYDExtForDsp.battri));
        }

        OpenRtb.BidResponse.Builder bidResponseBuilder = bidResponse.toBuilder().clearSeatbid();
        for (OpenRtb.BidResponse.SeatBid seatBid : bidResponse.getSeatbidList()) {
            OpenRtb.BidResponse.SeatBid.Builder validSeatBidBuilder = seatBid.toBuilder().clearBid();
            for (OpenRtb.BidResponse.SeatBid.Bid bid : seatBid.getBidList()) {
                if (isBidValid(bid, impIds, impId2Imps, impId2Bidfloors, impId2Battributes)) {
                    validSeatBidBuilder.addBid(bid.toBuilder());
                }
            }

            bidResponseBuilder.addSeatbid(validSeatBidBuilder);
        }

        return bidResponseBuilder.build();
    }


    private static boolean isBidValid(OpenRtb.BidResponse.SeatBid.Bid bid,
                                      List<String> impIds,
                                      Map<String, OpenRtb.BidRequest.Imp> impId2Imps,
                                      Map<String, Double> impId2Bidfloors,
                                      Map<String, List<Integer>> impId2Battributes) {
        return isValidImpId(bid, impIds) && isValidPrice(bid, impId2Bidfloors) && isValidAssets(bid, impId2Imps) && isValidAttrs(bid, impId2Battributes);
    }

    private static boolean isValidImpId(OpenRtb.BidResponse.SeatBid.Bid bid,
                                        List<String> impIds) {
        boolean valid = impIds.contains(bid.getImpid());
        if (!valid) {
            logger.warn("Imp id is invalid! Bid: \n" + bid);
        }
        return valid;
    }

    private static boolean isValidPrice(OpenRtb.BidResponse.SeatBid.Bid bid,
                                        Map<String, Double> impId2Bidfloors) {
        boolean valid = impId2Bidfloors.get(bid.getImpid()) <= bid.getPrice();
        if (!valid) {
            logger.warn("Price is under floor! Bid: \n" + bid);
        }
        return valid;
    }

    private static boolean isValidAssets(OpenRtb.BidResponse.SeatBid.Bid bid,
                                         Map<String, OpenRtb.BidRequest.Imp> impId2Imps) {
        OpenRtb.BidRequest.Imp requestImp = impId2Imps.get(bid.getImpid());
        if (requestImp == null) {
            logger.warn("ImpId invalid! " + bid.getImpid() + " are not in required ImpIds: " + impId2Imps.keySet());
            return false;
        }

        List<OpenRtb.NativeRequest.Asset> requestAssets = requestImp.getNative().getRequestNative().getAssetsList();
        if (requestAssets.isEmpty()) {
            logger.warn("Empty asset for Imp: " + requestImp);
            return false;
        }

        List<Integer> requestAssetIds = new ArrayList<Integer>(requestAssets.size());
        for (OpenRtb.NativeRequest.Asset asset : requestAssets) {
            requestAssetIds.add(asset.getId());
        }

        List<Integer> bidAssetIds = new ArrayList<Integer>();
        Map<Integer, OpenRtb.NativeResponse.Asset> bidAssetId2Asset = new HashMap<Integer, OpenRtb.NativeResponse.Asset>();
        for (OpenRtb.NativeResponse.Asset asset : bid.getAdmNative().getAssetsList()) {
            bidAssetIds.add(asset.getId());
            bidAssetId2Asset.put(asset.getId(), asset);
        }
        Collections.sort(requestAssetIds);
        Collections.sort(bidAssetIds);

        boolean valid = requestAssetIds.equals(bidAssetIds);
        if (!valid) {
            logger.warn("Bid's assetIds:" + bidAssetIds + " are not matched to required assets: " + requestAssetIds);
            return false;
        }

        for (OpenRtb.NativeRequest.Asset requestAsset : requestAssets) {
            OpenRtb.NativeResponse.Asset bidAsset = bidAssetId2Asset.get(requestAsset.getId());
            if (requestAsset.hasTitle()) {
                valid = bidAsset.hasTitle(); //&& bidAsset.getTitle().getText().length() <= requestAsset.getTitle().getLen();
            } else if (requestAsset.hasImg()) {
                valid = bidAsset.hasImg(); //&& bidAsset.getImg().getW() == requestAsset.getImg().getW() && bidAsset.getImg().getH() == requestAsset.getImg().getH();
            } else if (requestAsset.hasData()) {
                valid = bidAsset.hasData();
            } else {
                //TODO: Video
                valid = false;
            }

            if (!valid) {
                logger.warn("Bid's asset: " + bidAsset + " are not matched to required asset: " + requestAsset);
                return false;
            }
        }

        return true;
    }

    private static boolean isValidAttrs(OpenRtb.BidResponse.SeatBid.Bid bid,
                                        Map<String, List<Integer>> impId2Battributes) {
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

        String dataFormat = dataFormatOptionGroup.getSelected();
        // by default, data formatted by PB
        if (dataFormat == null) dataFormat = "pb";
        logger.info("dataFormat: " + dataFormat);

        OpenRtb.BidResponse bidResponse = sendBidRequest(bidServer, bidRequest, dataFormat);
        logger.info("Bid response: \n" + bidResponse);
    }


    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar target/yex-tool-1.0.0.jar ", "Tool to test Yex integration \n",
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

/**
 * @(#)YexOpenRtbJsonFactory, 16/6/15.
 * <p/>
 * Copyright 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.SetMultimap;
import com.google.openrtb.json.*;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author panpengfei.
 * @version 1.0.0
 */
public class YexOpenRtbJsonFactory extends OpenRtbJsonFactory {
    public YexOpenRtbJsonFactory(
            @Nullable JsonFactory jsonFactory,
            boolean strict,
            @Nullable SetMultimap<String, OpenRtbJsonExtReader<?>> extReaders,
            @Nullable Map<String, Map<String, Map<String, OpenRtbJsonExtWriter<?>>>> extWriters) {
        super(jsonFactory, strict, extReaders, extWriters);
    }

    /**
     * This "immutable-clone-constructor" returns an immutable copy of this factory.
     * Subclasses should have a protected constructor that super-calls this and makes
     * immutable copies of their own fields if necessary.  You can then override the
     * methods that create Reader/Writer objects so they use that constructor.
     */
    public YexOpenRtbJsonFactory(OpenRtbJsonFactory config) {
        super(config);
    }


    /**
     * Creates a new factory with default configuration.
     */
    public static YexOpenRtbJsonFactory create() {
        return new YexOpenRtbJsonFactory(null, true, null, null);
    }


    @Override
    public YexOpenRtbJsonReader newReader() {
        return new YexOpenRtbJsonReader(new YexOpenRtbJsonFactory(this));
    }

   @Override
    public YexOpenRtbJsonWriter newWriter() {
        return new YexOpenRtbJsonWriter(new YexOpenRtbJsonFactory(this));
    }

    @Override
    public YexOpenRtbNativeJsonWriter newNativeWriter() {
        return new YexOpenRtbNativeJsonWriter(new YexOpenRtbJsonFactory(this));
    }


    /**
     * Register an extension reader.
     *
     * @param extReader code to desserialize some extension properties
     * @param msgKlass  class of container message's builder, e.g. {@code MyImp.Builder.class}
     */
    public final <EB extends GeneratedMessage.ExtendableBuilder<?, EB>> YexOpenRtbJsonFactory yexRegister(
            OpenRtbJsonExtReader<EB> extReader, Class<EB> msgKlass) {
        return (YexOpenRtbJsonFactory) super.register(extReader, msgKlass);
    }

    /**
     * Register an extension writer, bound to a specific field name. This writer will be
     * used in preference to a non-field-specific writer that may exist for the same class.
     *
     * @param extWriter code to serialize some {@code extKlass}'s properties
     * @param extKlass  class of container message, e.g. {@code MyImp.class}
     * @param fieldName name of the field containing the extension
     * @param <T>       Type of value for the extension
     * @see #register(OpenRtbJsonExtWriter, Class, Class)
     */
    public final <T> YexOpenRtbJsonFactory yexRegister(OpenRtbJsonExtWriter<T> extWriter,
                                                       Class<T> extKlass, Class<? extends Message> msgKlass, String fieldName) {
        return (YexOpenRtbJsonFactory) super.register(extWriter, extKlass, msgKlass, fieldName);
    }
}

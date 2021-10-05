package com.mychat.constants;

import io.netty.util.AttributeKey;

public class ServerConstants {
    public static final String MANAGE_PATH = "/im/nodes";
    public static final String PATH_PREFIX = MANAGE_PATH + "/seq-";
    public static final String PATH_PREFIX_NO_STRIP =  "seq-";
    public static final String COUNTER_PATH = "/im/OnlineCounter";
    public static final String WEB_URL = "http://localhost:8080";
    public static final AttributeKey<String> CHANNEL_NAME = AttributeKey.valueOf("CHANNEL_NAME");
}
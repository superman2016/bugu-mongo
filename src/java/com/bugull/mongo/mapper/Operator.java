package com.bugull.mongo.mapper;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class Operator {
    
    public final static String ID = "_id";
    
    public static final String GT = "$gt";
    public static final String GTE = "$gte";
    public static final String LT = "$lt";
    public static final String LTE = "$lte";
    public static final String NE = "$ne";
    public static final String AND = "$and";
    public static final String OR = "$or";
    public static final String IN = "$in";
    public static final String NIN = "$nin";
    public static final String MOD = "$mod";
    public static final String ALL = "$all";
    public static final String SIZE = "$size";
    public static final String EXISTS = "$exists";
    public static final String REGEX = "$regex";
    
    public static final String NEAR = "$near";
    public static final String WITHIN = "$within";
    public static final String CENTER = "$center";
    public static final String BOX = "$box";
    
    public static final String SET = "$set";
    public static final String INC = "$inc";
    public static final String PUSH = "$push";
    public static final String PULL = "$pull";
}

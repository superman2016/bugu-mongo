/*
 * Copyright (c) www.bugull.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mongo.decoder;

import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.Property;
import com.bugull.mongo.utils.DataType;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
@SuppressWarnings("unchecked")
public class PropertyDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(PropertyDecoder.class);
    
    public PropertyDecoder(Field field, DBObject dbo){
        super(field);
        String fieldName = field.getName();
        Property property = field.getAnnotation(Property.class);
        if(property != null){
            String name = property.name();
            if(!name.equals(Default.NAME)){
                fieldName = name;
            }
        }
        value = dbo.get(fieldName);
    }
    
    @Override
    public void decode(Object obj){
        Class<?> type = field.getType();
        try{
            if(type.isArray()){
                Class comType = type.getComponentType();
                if(DataType.isByte(comType)){
                    decodeBinary(obj);
                }else{
                    decodeArray(obj, type.getComponentType());
                }
            }else{
                decodePrimitive(obj, type);
            }
        }catch(IllegalArgumentException ex){
            logger.error("Something is wrong when parse the field's value", ex);
        }catch(IllegalAccessException ex){
            logger.error("Something is wrong when parse the field's value", ex);
        }
    }
    
    private void decodeBinary(Object obj) throws IllegalArgumentException, IllegalAccessException {
        field.set(obj, (byte[])value);
    }
    
    private void decodeArray(Object obj, Class comType) throws IllegalArgumentException, IllegalAccessException {
        List list = (ArrayList)value;
        int size = list.size();
        if(DataType.isString(comType)){
            String[] arr = new String[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString();
            }
            field.set(obj, arr);
        }
        else if(DataType.isInteger(comType)){
            int[] arr = new int[size];
            for(int i=0; i<size; i++){
                arr[i] = Integer.parseInt(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isIntegerObject(comType)){
            Integer[] arr = new Integer[size];
            for(int i=0; i<size; i++){
                arr[i] = Integer.valueOf(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isLong(comType)){
            long[] arr = new long[size];
            for(int i=0; i<size; i++){
                arr[i] = Long.parseLong(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isLongObject(comType)){
            Long[] arr = new Long[size];
            for(int i=0; i<size; i++){
                arr[i] = Long.valueOf(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isShort(comType)){
            short[] arr = new short[size];
            for(int i=0; i<size; i++){
                arr[i] = Short.parseShort(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isShortObject(comType)){
            Short[] arr = new Short[size];
            for(int i=0; i<size; i++){
                arr[i] = Short.valueOf(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isByteObject(comType)){
            Byte[] arr = new Byte[size];
            for(int i=0; i<size; i++){
                arr[i] = Byte.valueOf(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isFloat(comType)){
            float[] arr = new float[size];
            for(int i=0; i<size; i++){
                arr[i] = Float.parseFloat(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isFloatObject(comType)){
            Float[] arr = new Float[size];
            for(int i=0; i<size; i++){
                arr[i] = Float.valueOf(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isDouble(comType)){
            double[] arr = new double[size];
            for(int i=0; i<size; i++){
                arr[i] = Double.parseDouble(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isDoubleObject(comType)){
            Double[] arr = new Double[size];
            for(int i=0; i<size; i++){
                arr[i] = Double.valueOf(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isBoolean(comType)){
            boolean[] arr = new boolean[size];
            for(int i=0; i<size; i++){
                arr[i] = Boolean.parseBoolean(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isBooleanObject(comType)){
            Boolean[] arr = new Boolean[size];
            for(int i=0; i<size; i++){
                arr[i] = Boolean.valueOf(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isChar(comType)){
            char[] arr = new char[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString().charAt(0);
            }
            field.set(obj, arr);
        }
        else if(DataType.isCharObject(comType)){
            Character[] arr = new Character[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString().charAt(0);
            }
            field.set(obj, arr);
        }
        else if(DataType.isDate(comType)){
            Date[] arr = new Date[size];
            for(int i=0; i<size; i++){
                arr[i] = (Date)list.get(i);
            }
            field.set(obj, arr);
        }
        else if(DataType.isTimestamp(comType)){
            Timestamp[] arr = new Timestamp[size];
            for(int i=0; i<size; i++){
                arr[i] = (Timestamp)list.get(i);
            }
            field.set(obj, arr);
        }
    }
    
    private void decodePrimitive(Object obj, Class type) throws IllegalArgumentException, IllegalAccessException{
        //When value is number, it's default to Double and Integer, must cast to Float, Short and byte.
        //It's OK to set integer value to long field.
        if(DataType.isFloat(type)){
            field.setFloat(obj, Float.parseFloat(value.toString()));
        }
        else if(DataType.isFloatObject(type)){
            field.set(obj, Float.valueOf(value.toString()));
        }
        else if(DataType.isShort(type)){
            field.setShort(obj, Short.parseShort(value.toString()));
        }
        else if(DataType.isShortObject(type)){
            field.set(obj, Short.valueOf(value.toString()));
        }
        else if(DataType.isByte(type)){
            field.setByte(obj, Byte.parseByte(value.toString()));
        }
        else if(DataType.isByteObject(type)){
            field.setByte(obj, Byte.valueOf(value.toString()));
        }
        else if(DataType.isListType(type)){
            List src = (ArrayList)value;
            List list = new ArrayList();
            moveCollectionElement(src, list);
            field.set(obj, list);
        }
        //convert for Set. default type is com.mongodb.BasicDBList(extends ArrayList)
        else if(DataType.isSetType(type)){
            List src = (ArrayList)value;
            Set set = new HashSet();
            moveCollectionElement(src, set);
            field.set(obj, set);
        }
        //convert for Queue. default type is com.mongodb.BasicDBList(extends ArrayList)
        else if(DataType.isQueueType(type)){
            List src = (ArrayList)value;
            Queue queue = new LinkedList();
            moveCollectionElement(src, queue);
            field.set(obj, queue);
        }
        //convert for char. default type is String "X"
        else if(DataType.isChar(type)){
            field.setChar(obj, value.toString().charAt(0));
        }
        //convert for Timestamp. default type is Date
        else if(DataType.isTimestamp(type)){
            Date date = (Date)value;
            Timestamp ts = new Timestamp(date.getTime());
            field.set(obj, ts);
        }
        else{
            field.set(obj, value);  //for others
        }
    }
    
    private void moveCollectionElement(List list, Collection collection){
        ParameterizedType paramType = (ParameterizedType)field.getGenericType();
        Type[] types = paramType.getActualTypeArguments();
        Class actualType = (Class)types[0];
        if(DataType.isShortObject(actualType)){
            for(Object o : list){
                collection.add(Short.valueOf(o.toString()));
            }
        }
        else if(DataType.isByteObject(actualType)){
            for(Object o : list){
                collection.add(Byte.valueOf(o.toString()));
            }
        }
        else if(DataType.isFloatObject(actualType)){
            for(Object o : list){
                collection.add(Float.valueOf(o.toString()));
            }
        }
        else if(DataType.isCharObject(actualType)){
            for(Object o : list){
                collection.add(o.toString().charAt(0));
            }
        }
        else{
            for(Object o : list){
                collection.add(o);
            }
        }
    }
    
}

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

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.ConstructorCache;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.mapper.DataType;
import com.bugull.mongo.mapper.FieldUtil;
import com.bugull.mongo.mapper.InternalDao;
import com.bugull.mongo.mapper.MapperUtil;
import com.bugull.mongo.mapper.Operator;
import com.bugull.mongo.mapper.ReferenceUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
@SuppressWarnings("unchecked")
public class RefListDecoder extends AbstractDecoder{
    
    private RefList refList;
    
    public RefListDecoder(Field field, DBObject dbo){
        super(field);
        refList = field.getAnnotation(RefList.class);
        String fieldName = field.getName();
        String name = refList.name();
        if(!name.equals(Default.NAME)){
            fieldName = name;
        }
        value = dbo.get(fieldName);
    }
    
    @Override
    public void decode(Object obj){
        Class<?> type = field.getType();
        if(type.isArray()){
            decodeArray(obj, type.getComponentType());
        }else{
            ParameterizedType paramType = (ParameterizedType)field.getGenericType();
            Type[] types = paramType.getActualTypeArguments();
            int len = types.length;
            if(len == 1){
                decodeListAndSet(obj, (Class)types[0]);
            }else if(len == 2){
                decodeMap(obj, (Class)types[1]);
            }
        }
    }
    
    private void decodeArray(Object obj, Class clazz){
        clazz = FieldUtil.getRealType(clazz);
        List list = (ArrayList)value;
        int size = list.size();
        if(size <= 0){
            return;
        }
        Object arr = Array.newInstance(clazz, size);
        if(refList.cascade().toUpperCase().indexOf(Default.CASCADE_READ)==-1){
            for(int i=0; i<size; i++){
                Object item = list.get(i);
                if(item != null){
                    String refId = ReferenceUtil.fromDbReference(refList, item);
                    BuguEntity refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
                    refObj.setId(refId);
                    Array.set(arr, i, refObj);
                }else{
                    Array.set(arr, i, null);
                }
            }
        }
        else{
            List<Object> idList = new ArrayList<Object>();
            for(int i=0; i<size; i++){
                Object item = list.get(i);
                if(item != null){
                    Object refId = ReferenceUtil.toDbReference(refList, clazz, item);
                    idList.add(refId);
                }
            }
            DBObject in = new BasicDBObject(Operator.IN, idList);
            DBObject query = new BasicDBObject(Operator.ID, in);
            InternalDao dao = DaoCache.getInstance().get(clazz);
            String sort = refList.sort();
            List<BuguEntity> entityList = null;
            if(sort.equals(Default.SORT)){
                entityList = dao.find(query);
            }else{
                entityList = dao.find(query, MapperUtil.getSort(sort));
            }
            if(entityList.size() != size){
                size = entityList.size();
                arr = Array.newInstance(clazz, size);
            }
            for(int i=0; i<size; i++){
                Array.set(arr, i, entityList.get(i));
            }
        }
        FieldUtil.set(obj, field, arr);
    }
    
    private void decodeListAndSet(Object obj, Class clazz){
        clazz = FieldUtil.getRealType(clazz);
        List list = (List)value;
        List<BuguEntity> result = new ArrayList<BuguEntity>();
        if(refList.cascade().toUpperCase().indexOf(Default.CASCADE_READ)==-1){
            for(Object item : list){
                if(item != null){
                    String refId = ReferenceUtil.fromDbReference(refList, item);
                    BuguEntity refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
                    refObj.setId(refId);
                    result.add(refObj);
                }
            }
        }else{
            List<Object> idList = new ArrayList<Object>();
            for(Object item : list){
                if(item != null){
                    Object refId = ReferenceUtil.toDbReference(refList, clazz, item);
                    idList.add(refId);
                }
            }
            DBObject in = new BasicDBObject(Operator.IN, idList);
            DBObject query = new BasicDBObject(Operator.ID, in);
            InternalDao dao = DaoCache.getInstance().get(clazz);
            String sort = refList.sort();
            if(sort.equals(Default.SORT)){
                result = dao.find(query);
            }else{
                result = dao.find(query, MapperUtil.getSort(sort));
            }
        }
        Class type = field.getType();
        if(DataType.isList(type)){
            FieldUtil.set(obj, field, result);
        }
        else if(DataType.isSet(type)){
            FieldUtil.set(obj, field, new HashSet(result));
        }
    }
    
    private void decodeMap(Object obj, Class clazz){
        clazz = FieldUtil.getRealType(clazz);
        Map map = (Map)value;
        Map<Object, BuguEntity> result = new HashMap<Object, BuguEntity>();
        if(refList.cascade().toUpperCase().indexOf(Default.CASCADE_READ)==-1){
            for(Object key : map.keySet()){
                Object item = map.get(key);
                if(item != null){
                    String refId = ReferenceUtil.fromDbReference(refList, item);
                    BuguEntity refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
                    refObj.setId(refId);
                    result.put(key, refObj);
                }else{
                    result.put(key, null);
                }
            }
        }else{
            InternalDao dao = DaoCache.getInstance().get(clazz);
            for(Object key : map.keySet()){
                Object item = map.get(key);
                if(item != null){
                    String refId = ReferenceUtil.fromDbReference(refList, item);
                    BuguEntity refObj = (BuguEntity)dao.findOne(refId);
                    result.put(key, refObj);
                }else{
                    result.put(key, null);
                }
            }
        }
        FieldUtil.set(obj, field, result);
    }
    
}

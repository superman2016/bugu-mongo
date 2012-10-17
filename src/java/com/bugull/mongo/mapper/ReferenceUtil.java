/**
 * Copyright (c) www.bugull.com
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

package com.bugull.mongo.mapper;

import com.bugull.mongo.BuguConnection;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.exception.DBConnectionException;
import com.bugull.mongo.exception.FieldException;
import com.bugull.mongo.exception.IdException;
import com.mongodb.DB;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ReferenceUtil {
    
    private final static Logger logger = Logger.getLogger(ReferenceUtil.class);
    
    public static Object toDbReference(Ref ref, Class<?> clazz, String idStr){
        if(StringUtil.isEmpty(idStr)){
            return null;
        }
        Object result = null;
        if(ref.manual()){
            result = toManualRef(clazz, idStr);
        }else{
            result = toDBRef(clazz, idStr);
        }
        return result;
    }
    
    public static Object toDbReference(RefList refList, Class<?> clazz, String idStr){
        if(StringUtil.isEmpty(idStr)){
            return null;
        }
        Object result = null;
        if(refList.manual()){
            result = toManualRef(clazz, idStr);
        }else{
            result = toDBRef(clazz, idStr);
        }
        return result;
    }
    
    private static Object toManualRef(Class<?> clazz, String idStr){
        Object result = null;
        Field idField = null;
        try{
            idField = FieldsCache.getInstance().getIdField(clazz);
        }catch(IdException ex){
            logger.error(ex.getMessage(), ex);
        }
        Id idAnnotation = idField.getAnnotation(Id.class);
        switch(idAnnotation.type()){
            case AUTO_GENERATE:
                result = new ObjectId(idStr);
                break;
            case AUTO_INCREASE:
                result = Long.parseLong(idStr);
                break;
            case USER_DEFINE:
                result = idStr;
                break;
        }
        return result;
    }
    
    private static DBRef toDBRef(Class<?> clazz, String idStr){
        DB db = null;
        try {
            db = BuguConnection.getInstance().getDB();
        } catch (DBConnectionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        String name = MapperUtil.getEntityName(clazz);
        Object dbId = IdUtil.toDbId(clazz, idStr);
        return new DBRef(db, name, dbId);
    }
    
    public static String fromDbReference(Ref ref, Object value){
        String result = null;
        if(ref.manual()){
            result = value.toString();
        }else{
            DBRef dbRef = (DBRef)value;
            result = dbRef.getId().toString();
        }
        return result;
    }
    
    public static String fromDbReference(RefList refList, Object value){
        String result = null;
        if(refList.manual()){
            result = value.toString();
        }else{
            DBRef dbRef = (DBRef)value;
            result = dbRef.getId().toString();
        }
        return result;
    }
    
    public static Object toDbReference(RefList refList, Class<?> clazz, Object value){
        String idStr = fromDbReference(refList, value);
        return toDbReference(refList, clazz, idStr);
    }
    
    public static Object toDbReference(Class<?> clazz, String fieldName, Class<?> refClass, String idStr){
        Object result = null;
        Field refField = null;
        try{
            refField = FieldsCache.getInstance().getField(clazz, fieldName);
        }catch(FieldException ex){
            logger.error(ex.getMessage(), ex);
        }
        Ref ref = refField.getAnnotation(Ref.class);
        if(ref != null){
            result = toDbReference(ref, refClass, idStr);
        }else{
            RefList refList = refField.getAnnotation(RefList.class);
            result = toDbReference(refList, refClass, idStr);
        }
        return result;
    }

}

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

package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.Compare;
import com.bugull.mongo.lucene.annotations.IndexFilter;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexFilterChecker {
    
    private BuguEntity obj;
    
    public IndexFilterChecker(BuguEntity obj){
        this.obj = obj;
    }
    
    public boolean needIndex(){
        Class<?> clazz = obj.getClass();
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field f : fields){
            IndexFilter filter = f.getAnnotation(IndexFilter.class);
            if(filter != null){
                Compare compare = filter.compare();
                String value = filter.value();
                CompareChecker checker = new CompareChecker(obj);
                if(! checker.isFit(f, compare, value)){
                    return false;
                }
            }
        }
        return true;
    }
    
}

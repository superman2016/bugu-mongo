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

package com.bugull.mongo.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexSearcherCache {
    
    private final static Logger logger = Logger.getLogger(IndexSearcherCache.class);
    
    private static IndexSearcherCache instance = new IndexSearcherCache();
    
    private Map<String, IndexSearcher> cache;
    private Map<String, Boolean> openning;
    
    private IndexSearcherCache(){
        cache = new ConcurrentHashMap<String, IndexSearcher>();
        openning = new ConcurrentHashMap<String, Boolean>();
    }
    
    public static IndexSearcherCache getInstance(){
        return instance;
    }
    
    public IndexSearcher get(String name){
        IndexSearcher searcher = null;
        if(cache.containsKey(name)){
            searcher = cache.get(name);
        }else{
            synchronized(name){
                if(cache.containsKey(name)){
                    searcher = cache.get(name);
                }else{
                    IndexWriter writer = IndexWriterCache.getInstance().get(name);
                    IndexReader reader = null;
                    try{
                        reader = IndexReader.open(writer, true);
                    }catch(Exception e){
                        logger.error(e.getMessage());
                    }
                    searcher = new IndexSearcher(reader);
                    cache.put(name, searcher);
                }
            }
        }
        return searcher;
    }
    
    public Map<String, IndexSearcher> getAll(){
        return cache;
    }
    
    public void put(String name, IndexSearcher searcher){
        cache.put(name, searcher);
    }
    
    public Boolean isOpenning(String name){
        if(openning.containsKey(name)){
            return openning.get(name);
        }else{
            return Boolean.FALSE;
        }
    }
    
    public void putOpenning(String name, Boolean value){
        openning.put(name, value);
    }
    
}

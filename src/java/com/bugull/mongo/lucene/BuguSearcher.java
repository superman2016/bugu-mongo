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

package com.bugull.mongo.lucene;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexSearcherCache;
import com.bugull.mongo.mapper.MapperUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguSearcher {
    
    private final static Logger logger = Logger.getLogger(BuguSearcher.class);
    
    private Class<?> clazz;
    private IndexSearcher searcher;
    private IndexReader reader;
    
    private Query query;
    private Sort sort;
    private Filter filter;
    private int pageNumber = 1;
    private int pageSize = 20;
    private int maxPage = 50;
    private int resultCount;
    private BuguHighlighter highlighter;
    
    public BuguSearcher(Class<?> clazz){
        this.clazz = clazz;
        String name = MapperUtil.getEntityName(clazz);
        searcher = IndexSearcherCache.getInstance().get(name);
        reader = searcher.getIndexReader();
        reader.incRef();
    }
    
    public BuguSearcher setQuery(Query query){
        this.query = query;
        return this;
    }
    
    public BuguSearcher setSort(Sort sort){
        this.sort = sort;
        return this;
    }
    
    public BuguSearcher setFilter(Filter filter){
        this.filter = filter;
        return this;
    }
    
    public BuguSearcher setMaxPage(int maxPage){
        this.maxPage = maxPage;
        return this;
    }
    
    public BuguSearcher setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
        return this;
    }
    
    public BuguSearcher setPageSize(int pageSize){
        this.pageSize = pageSize;
        return this;
    }
    
    public BuguSearcher setHighlighter(BuguHighlighter highlighter) {
        this.highlighter = highlighter;
        return this;
    }
    
    public int getResultCount(){
        return resultCount;
    }
    
    public List search(Query query){
        this.query = query;
        return search();
    }
    
    public List search(Query query, Sort sort){
        this.query = query;
        this.sort = sort;
        return search();
    }
    
    public List search(Query query, Filter filter){
        this.query = query;
        this.filter = filter;
        return search();
    }
    
    public List search(Query query, Filter filter, Sort sort){
        this.query = query;
        this.filter = filter;
        this.sort = sort;
        return search();
    }
    
    public List search(){
        TopDocs topDocs = null;
        try{
            if(sort == null){
                topDocs = searcher.search(query, filter, maxPage*pageSize);
            }else{
                topDocs = searcher.search(query, filter, maxPage*pageSize, sort);
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        if(topDocs == null){
            return Collections.emptyList();
        }
        resultCount = topDocs.totalHits;
        ScoreDoc[] docs = topDocs.scoreDocs;
        List list = new ArrayList();
        BuguDao dao = DaoCache.getInstance().get(clazz);
        int begin = (pageNumber - 1) * pageSize;
        int end = begin + pageSize;
        if(end > resultCount){
            end = resultCount;
        }
        for(int i=begin; i<end; i++){
            try{
                Document doc = searcher.doc(docs[i].doc);
                String id = doc.get(FieldsCache.getInstance().getIdFieldName(clazz));
                list.add(dao.findOne(id));
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        //process highlighter
        if(highlighter != null){
            for(Object obj : list){
                String[] fields = highlighter.getFields();
                for(String fieldName : fields){
                    try{
                        Field field = FieldsCache.getInstance().getField(clazz, fieldName);
                        String fieldValue = field.get(obj).toString();
                        field.set(obj, highlighter.getResult(fieldName, fieldValue));
                    }catch(Exception e){
                        logger.error(e.getMessage());
                    }
                }
            }
        }
        return list;
    }
    
    public void close(){
        try{
            reader.decRef();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    public IndexSearcher getSearcher(){
        return searcher;
    }

}
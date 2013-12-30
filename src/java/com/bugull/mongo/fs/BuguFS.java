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

package com.bugull.mongo.fs;

import com.bugull.mongo.BuguConnection;
import com.bugull.mongo.exception.DBConnectionException;
import com.bugull.mongo.utils.MapperUtil;
import com.bugull.mongo.utils.Operator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

/**
 * Basic class for operating the GridFS.
 * 
 * <p>BuguFS uses the BuguConnection class internally, so you don't need to care about the connetion and collections of GridFS.</p>
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguFS {
    
    private final static Logger logger = Logger.getLogger(BuguFS.class);
    
    private GridFS fs;
    private DBCollection files;
    private String bucketName;
    private long chunkSize;
    
    public final static String BUCKET = "bucket";
    public final static String FILENAME = "filename";
    
    public BuguFS(String bucketName, long chunkSize){
        this.bucketName = bucketName;
        this.chunkSize = chunkSize;
        DB db = null;
        try {
            db = BuguConnection.getInstance().getDB();
        } catch (DBConnectionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        fs = new GridFS(db, bucketName);
        files = db.getCollection(bucketName + ".files");
        //ensure the DBCursor can be cast to GridFSDBFile
        files.setObjectClass(GridFSDBFile.class);
    }
    
    public GridFS getGridFS(){
        return fs;
    }
    
    public void save(File file){
        save(file, file.getName(), null);
    }
    
    public void save(File file, String filename){
        save(file, filename, null);
    }
    
    public void save(File file, String filename, Map<String, Object> attributes){
        GridFSInputFile f = null;
        try{
            f = fs.createFile(file);
        }catch(IOException ex){
            logger.error("Can not create GridFSInputFile", ex);
        }
        f.setChunkSize(chunkSize);
        f.setFilename(filename);
        setAttributes(f, attributes);
        f.save();
    }
    
    public void save(InputStream is, String filename){
        save(is, filename, null);
    }
    
    public void save(InputStream is, String filename, Map<String, Object> attributes){
        GridFSInputFile f = fs.createFile(is);
        f.setChunkSize(chunkSize);
        f.setFilename(filename);
        setAttributes(f, attributes);
        f.save();
    }
    
    public void save(byte[] data, String filename){
        save(data, filename, null);
    }
    
    public void save(byte[] data, String filename, Map<String, Object> attributes){
        GridFSInputFile f = fs.createFile(data);
        f.setChunkSize(chunkSize);
        f.setFilename(filename);
        setAttributes(f, attributes);
        f.save();
    }
    
    private void setAttributes(GridFSInputFile f, Map<String, Object> attributes){
        if(attributes != null){
            for(Entry<String, Object> entry : attributes.entrySet()){
                f.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public GridFSDBFile findOne(String filename){
        return fs.findOne(filename);
    }
    
    public GridFSDBFile findOne(DBObject query){
        return fs.findOne(query);
    }
    
    public List<GridFSDBFile> find(DBObject query){
        return fs.find(query);
    }
    
    public List<GridFSDBFile> find(DBObject query, String orderBy){
        DBObject sort = MapperUtil.getSort(orderBy);
        return fs.find(query, sort);
    }
    
    public List<GridFSDBFile> find(DBObject query, int pageNum, int pageSize){
        DBCursor cursor = files.find(query).skip((pageNum-1)*pageSize).limit(pageSize);
        return toFileList(cursor);
    }
    
    public List<GridFSDBFile> find(DBObject query, String orderBy, int pageNum, int pageSize){
        DBObject sort = MapperUtil.getSort(orderBy);
        DBCursor cursor = files.find(query).sort(sort).skip((pageNum-1)*pageSize).limit(pageSize);
        return toFileList(cursor);
    }
    
    public void rename(String oldName, String newName){
        DBObject query = new BasicDBObject(FILENAME, oldName);
        DBObject dbo = files.findOne(query);
        dbo.put(FILENAME, newName);
        files.save(dbo);
    }
    
    public void rename(GridFSDBFile file, String newName){
        ObjectId id = (ObjectId)file.getId();
        DBObject query = new BasicDBObject(Operator.ID, id);
        DBObject dbo = files.findOne(query);
        dbo.put(FILENAME, newName);
        files.save(dbo);
    }
    
    public void remove(String filename){
        fs.remove(filename);
    }
    
    public void remove(DBObject query){
        fs.remove(query);
    }
    
    private List<GridFSDBFile> toFileList(DBCursor cursor){
        List<GridFSDBFile> list = new ArrayList<GridFSDBFile>();
        while(cursor.hasNext()){
            DBObject dbo = cursor.next();
            list.add((GridFSDBFile)dbo);
        }
        cursor.close();
        return list;
    }

    public String getBucketName() {
        return bucketName;
    }

    public long getChunkSize() {
        return chunkSize;
    }
    
}

/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */


package org.mule.module.mongo.tools;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBDecoder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public class RestoreFile implements Comparable<RestoreFile>
{
    private String collection;
    private File file;

    public RestoreFile(File file)
    {
        this.file = file;
        this.collection = BackupUtils.getCollectionName(file.getName());
    }

    public List<DBObject> getCollectionObjects() throws IOException
    {
        BSONDecoder bsonDecoder = new DefaultDBDecoder();
        BufferedInputStream inputStream = null;
        List<DBObject> dbObjects = new ArrayList<DBObject>();

        try
        {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            while(inputStream.available() != 0)
            {
                BSONObject bsonObject = bsonDecoder.readObject(inputStream);
                if(bsonObject != null)
                {
                    dbObjects.add(new BasicDBObject((BasicBSONObject) bsonObject));
                }
            }
            return dbObjects;
        }
        finally
        {
            if(inputStream != null)
            {
                inputStream.close();
            }
        }
    }

    public String getCollection()
    {
        return collection;
    }
    
    public int compareTo(RestoreFile restoreFile)
    {
        return collection.compareTo(restoreFile.getCollection());
    }
    
    @Override
    public boolean equals(Object obj)
    {
    	if (this == obj)
    	{
            return true;
    	}
    	
        if (obj == null)
        {
            return false;
        }
        
        if (getClass() != obj.getClass())
        {
            return false;
        }
        
        RestoreFile other = (RestoreFile) obj;
        if (file == null)
        {
            if (other.file != null)
            {
                return false;
            }
        }
        
        if (collection == null)
        {
        	if (other.collection != null)
        	{
        		return false;
        	}
        } 
        else if (!file.equals(other.file))
        {
            return false;
        }
        else if (!collection.equals(other.collection))
        {
        	return false;
        }
        
        return true;
    }
}

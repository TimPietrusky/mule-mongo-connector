/**
 * Mule MongoDB Cloud Connector
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.mongo.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.UnhandledException;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Conversions between JSon Strings and Maps into DBObjects  
 */
public final class DBObjects
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("ObjectId\\((.+)\\)");

    private DBObjects()
    {
    }

    /**
     * Performs a shallow conversion of a map into a DBObject: values of type Map
     * will not be converted
     */
    public static DBObject fromMap(Map<String, Object> map)
    {
        return new BasicDBObject(map);
    }

    @SuppressWarnings("unchecked")
    public static DBObject from(Object o)
    {
        if (o == null)
        {
            return null;
        }
        if (o instanceof String)
        {
            return fromJson((String) o);
        }
        if (o instanceof DBObject)
        {
            return (DBObject) o;
        }
        if (o instanceof Map<?, ?>)
        {
            return fromMap((Map<String, Object>) o);
        }
        throw new IllegalArgumentException("Unsupported object type " + o);
    }

    public static DBObject fromJson(String json)
    {
        if (json.length() == 0)
        {
            return new BasicDBObject();
        }
        try
        {
            return (DBObject) adapt(MAPPER.readValue(json, BasicDBObject.class));
        }
        catch (Exception e)
        {
            throw new UnhandledException("Could not parse JSon " + json, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Object adapt(Object o)
    {
        if (o instanceof DBObject)
        {
            adaptObjectId((DBObject) o);
            adaptAttributes((DBObject) o);
        }
        else if (o instanceof Map<?, ?>)
        {
            o = adapt(fromMap((Map<String, Object>) o));
        }
        else if (o instanceof List<?>)
        {
            adaptElements(o);
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    private static void adaptElements(Object o)
    {
        for (ListIterator<Object> iter = ((List<Object>) o).listIterator(); iter.hasNext();)
        {
            iter.set(adapt(iter.next()));
        }
    }

    private static void adaptAttributes(DBObject o)
    {
        for (String key : o.keySet())
        {
            o.put(key, adapt(o.get(key)));
        }
    }

    private static void adaptObjectId(DBObject o)
    {
        Object id = o.get("_id");
        Matcher m;
        if (id != null && id instanceof String && (m = objectIdMatcher(id)).matches())
        {
            o.put("_id", new ObjectId(m.group(1)));
        }
    }

    private static Matcher objectIdMatcher(Object id)
    {
        return OBJECT_ID_PATTERN.matcher((String) id);
    }
}

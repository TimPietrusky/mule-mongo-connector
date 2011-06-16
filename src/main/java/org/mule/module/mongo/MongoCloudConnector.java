/**
 * Mule MongoDB Cloud Connector
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

/**
 * This file was automatically generated by the Mule Cloud Connector Development Kit
 */
package org.mule.module.mongo;

import static org.mule.module.mongo.api.DBObjects.from;

import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.module.mongo.api.IndexOrder;
import org.mule.module.mongo.api.MongoClient;
import org.mule.module.mongo.api.MongoClientImpl;
import org.mule.module.mongo.api.WriteConcern;
import org.mule.tools.cloudconnect.annotations.Connector;
import org.mule.tools.cloudconnect.annotations.Operation;
import org.mule.tools.cloudconnect.annotations.Parameter;
import org.mule.tools.cloudconnect.annotations.Property;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
/**
 * A Mongo Connector Facade
 * @author flbulgarelli
 */
@Connector(namespacePrefix = "mongo")
public class MongoCloudConnector implements Initialisable
{
    @Property(name = "client-ref", optional = true)
    private MongoClient client;
    /**
     * The host of the Mongo server
     */
    @Property(optional = true, defaultValue = "localhost")
    private String host;
    /**
     * The port of the Mongo server
     */
    @Property(optional = true, defaultValue = "27017")
    private int port;
    /**
     * The database name of the Mongo server
     */
    @Property(optional = true, defaultValue = "test")
    private String database;
    /**
     * The user password. Only required for collections that require authentication
     */
    @Property(optional = true)
    private String password;
    /**
     * The user name. Only required for collections that require authentication
     */
    @Property(optional = true)
    private String username;
    
    private static final Map<MongoRef, Mongo> MONGOS = new HashMap<MongoRef, Mongo>();
    
    
    /**
     * Lists names of collections available at this database
     * 
     * {@code <list-collections/>}
     * @return the list of names of collections available at this database
     */
    @Operation
    public Collection<String> listCollections()
    {
        return client.listCollections();
    }

    /**
     * Answers if a collection exists given its name
     * 
     * {@code <exists-collection name="aColllection"/>}
     * @param collection the name of the collection
     * @return if the collection exists 
     */
    @Operation
    public boolean existsCollection(@Parameter String collection)
    {
        return client.existsCollection(collection);
    }

    /**
     * Deletes a collection and all the objects it contains.  
     * If the collection does not exist, does nothing.
     * 
     * {@code <drop-collection name="aCollection"/>}
     * @param collection the name of the collection to drop
     */
    @Operation
    public void dropCollection(@Parameter String collection)
    {
        client.dropCollection(collection);
    }

    /**
     * Creates a new collection. 
     * If the collection already exists, a MongoException will be thrown.
     * 
     * {@code <create-collection name="aCollection" capped="true"/>}
     * 
     * @param collection the name of the collection to create
     * @param capped if the collection will be capped 
     * @param maxObjects the maximum number of documents the new collection is able to contain
     * @param size the maximum size of the new collection 
     */
    @Operation
    public void createCollection(@Parameter String collection,
                                 @Parameter(optional = true, defaultValue = "false") boolean capped,
                                 @Parameter(optional = true) Integer maxObjects,
                                 @Parameter(optional = true) Integer size)
    {
        client.createCollection(collection, capped, maxObjects, size);
    }
    
    /**
     * Inserts an object in a collection, setting its id if necessary.
     * 
     * Object can either be a raw DBObject, a String-Object Map or a JSon String.
     * If it is passed as Map, a shallow conversion into DBObject is performed - that is, no conversion is performed to its values.
     * If it is passed as JSon String, _ids of type ObjectId must be passed as a String, for example: 
     * { "_id": "ObjectId(4df7b8e8663b85b105725d34)", "foo" : 5, "bar": [ 1 , 2 ] }
     * 
     * {@code <insert-object collection="Employees" object="#[header:aBsonEmployee]" writeConcern="SAFE"/>}
     * @param collection the name of the collection where to insert the given object
     * @param element the object to insert. Maps, JSon Strings and DBObjects are supported.
     * @param elementAttributes alternative way of specifying the element as a literal Map inside a Mule Flow
     * @param writeConcern the optional write concern of insertion
     */
    @Operation
    public void insertObject(@Parameter String collection,
                             @Parameter(optional = true) Object element,
                             @Parameter(optional = true) Map<String, Object> elementAttributes,
                             @Parameter(optional = true, defaultValue = "DATABASE_DEFAULT") WriteConcern writeConcern)
    {
        client.insertObject(collection, from(single(elementAttributes, element)), writeConcern);
    }

    /**
     * Updates objects that matches the given query. If parameter multi is set to false,
     * only the first document matching it will be updated. 
     * Otherwise, all the documents matching it will be updated.  
     * 
     * {@code <update-objects collection="#[map-payload:aCollectionName]" 
     *         query="#[variable:aBsonQuery]" object="#[variable:aBsonObject]" upsert="true"/>} 
     * @param collection the name of the collection to update
     * @param query the query object used to detect the element to update. Maps, JSon Strings and DBObjects are supported, as described in insert-object operation.
     * @param queryAttributes alternative way of passing query as a literal Map inside a Mule flow
     * @param element the mandatory object that will replace that one which matches the query. Maps, JSon Strings and DBObjects are supported, as described in insert-object operation.
     * @param elementAttributes alternative way of specifying the element as a literal Map inside a Mule Flow 
     * @param upsert if the database should create the element if it does not exist
     * @param multi if all or just the first object matching the query will be updated
     * @param writeConcern the write concern used to update 
     */
    @Operation
    public void updateObjects(@Parameter String collection,
                              @Parameter( optional = true) Object query,
                              @Parameter(optional = true) Map<String, Object> queryAttributes,
                              @Parameter(optional = true) Object element,
                              @Parameter(optional = true) Map<String, Object> elementAttributes,
                              @Parameter(optional = true, defaultValue = "false") boolean upsert,
                              @Parameter(optional = true, defaultValue = "true") boolean multi,
                              @Parameter(optional = true, defaultValue = "DATABASE_DEFAULT") WriteConcern writeConcern)
    {
        client.updateObjects(collection, from(single(queryAttributes, query)), from(single(elementAttributes, element)),
            upsert, multi, writeConcern);
    }

    /**
     * Inserts or updates an object based on its object _id.
     *  
     * {@code <save-object 
     *          collection="#[map-payload:aCollectionName]"
     *          object="#[header:aBsonObject]"/>} 
     * @param collection the collection where to insert the object
     * @param element the mandatory object to insert. Maps, JSon Strings and DBObjects are supported, as described in insert-object operation.
     * @param elementAttributes an alternative way of passing the element as a literal Map inside a Mule Flow
     * @param writeConcern the write concern used to persist the object
     */
    @Operation
    public void saveObject(@Parameter String collection,
                           @Parameter(optional = true) Object element,
                           @Parameter(optional = true) Map<String, Object> elementAttributes,
                           @Parameter(optional = true, defaultValue = "DATABASE_DEFAULT") WriteConcern writeConcern)
    {
        client.saveObject(collection, from(single(elementAttributes, element)), writeConcern);
    }

    /**
     * Removes all the objects that match the a given optional query. 
     * If query is not specified, all objects are removed. However, please notice that this is normally
     * less performant that dropping the collection and creating it and its indices again
     * 
     * {@code <remove-objects collection="#[map-payload:aCollectionName]" query="#[map-payload:aBsonQuery]"/>}
     * @param collection the collection whose elements will be removed 
     * @param query the optional query object. Objects that match it will be removed. Maps, JSon Strings and DBObjects are supported, as described in insert-object operation.
     * @param queryAttributes an alternative way of passing the query as a literal Map inside a Mule Flow
     * @param writeConcern the write concern used to remove the object
     */
    @Operation
    public void removeObjects(@Parameter String collection,
                              @Parameter( optional = true) Object query,
                              @Parameter(optional = true) Map<String, Object> queryAttributes,
                              @Parameter(optional = true, defaultValue = "DATABASE_DEFAULT") WriteConcern writeConcern)
    {
        client.removeObjects(collection, from(single(queryAttributes, query)), writeConcern);
    }
    
    /**
     * Transforms a collection into a collection of aggregated groups, by
     * applying a supplied element-mapping function to each element, that transforms each one
     * into a key-value pair, grouping the resulting pairs by key, and finally 
     * reducing values in each group applying a suppling 'reduce' function.   
     * 
     * Each supplied function is coded in JavaScript.
     * 
     * Note that the correct way of writing those functions may not be obvious; please 
     * consult MongoDB documentation for writing them.
     *  
     * {@code  <map-reduce-objects 
     *      collection="myCollection"
     *      mapFunction="#[header:aJSMapFunction]"
     *      reduceFunction="#[header:aJSReduceFunction]"/>} 
     * @param collection the name of the collection to map and reduce
     * @param mapFunction a JavaScript encoded mapping function
     * @param reduceFunction a JavaScript encoded reducing function 
     * @param outputCollection the name of the output collection to write the results, replacing previous collection if existed,
     *          mandatory when results may be larger than 16MB. 
     *          If outputCollection is unspecified, the computation is performed in-memory and not persisted.
     * @return an iterable that retrieves the resulting collection DBObjects   
     */
    @Operation
    public Iterable<DBObject> mapReduceObjects(@Parameter String collection,
                                               @Parameter String mapFunction,
                                               @Parameter String reduceFunction,
                                               @Parameter(optional = true) String outputCollection)
    {
        return client.mapReduceObjects(collection, mapFunction, reduceFunction, outputCollection);
    }
    
    /**
     * Counts the number of objects that match the given query. If no query
     * is passed, returns the number of elements in the collection
     * 
     * {@code <count-objects 
     *      collection="#[variable:aCollectionName]"
     *      query="#[variable:aBsonQuery]"/>}
     *      
     * @param collection the target collection  
     * @param query the optional query for counting objects. Only objects matching it will be counted. 
     *          If unspecified, all objects are counted. Maps, JSon Strings and DBObjects are supported, as described in insert-object operation.
     * @param queryAttributes an alternative way of passing the query as a literal Map inside a Mule Flow    
     * @return the amount of objects that matches the query     
     */
    @Operation
    public long countObjects(@Parameter String collection,
                             @Parameter( optional = true) Object query,
                             @Parameter(optional = true) Map<String, Object> queryAttributes)
    {
        return client.countObjects(collection, from(single(queryAttributes, query)));
    }

    /**
     * Finds all objects that match a given query. If no query is specified, all objects of the 
     * collection are retrieved. If no fields object is specified, all fields are retrieved. 
     * 
     * {@code <find-objects query="#[map-payload:aBsonQuery]" fields-ref="#[header:aBsonFieldsSet]"/>}
     * @param collection the target collection
     * @param query the optional query object. If unspecified, all documents are returned. Maps, JSon Strings and DBObjects are supported, as described in insert-object operation.
     * @param queryAttributes alternative way of passing the query object, as a literal Map inside a Mule Flow
     * @param fieldsRef an optional list of fields to return. If unspecified, all fields are returned. 
     * @param fields alternative way of passing fields as a literal List
     * @return an iterable of DBObjects
     */
    @Operation
    public Iterable<DBObject> findObjects(@Parameter String collection,
                                          @Parameter( optional = true) Object query,
                                          @Parameter(optional = true) Map<String, Object> queryAttributes,
                                          @Parameter(optional = true) Object fieldsRef,
                                          @Parameter(optional = true) List<String> fields)
    {
        return client.findObjects(collection, from(single(queryAttributes, query)), single(fieldsRef, fields));
    }

    /**
     * Finds the first object that matches a given query. 
     * Throws a {@link MongoException} if no one matches the given query 
     * 
     * {@code <find-one-object  query="#[variable:aBsonQuery]" >
     *         <fields>
     *           <field>Field1</field>
     *           <field>Field2</field>
     *         </fields>
     *   </find-one-object>}   
     * @param collection the target collection
     * @param query the mandatory query object that the returned object matches. Maps, JSon Strings and DBObjects are supported, as described in insert-object operation.
     * @param queryAttributes alternative way of passing the query object, as a literal Map inside a Mule Flow
     * @param fieldsRef an optional list of fields to return. If unspecified, all fields are returned. 
     * @param fields alternative way of passing fields as a literal List
     * @return a non-null DBObject that matches the query. 
     */
    @Operation
    public DBObject findOneObject(@Parameter String collection,
                                  @Parameter(optional = true) Object query,
                                  @Parameter(optional = true) Map<String, Object> queryAttributes,
                                  @Parameter(optional = true) Object fieldsRef, 
                                  @Parameter(optional = true) List<String> fields) 
    {
        return client.findOneObject(collection, from(single(queryAttributes, query)), single(fieldsRef, fields));

    }
    
    /**
     * Creates a new index
     * 
     * {@code <create-index collection="myCollection" keys="#[header:aBsonFieldsSet]"/>}
     * @param collection the name of the collection where the index will be created
     * @param field the name of the field which will be indexed 
     * @param order the indexing order
     */
    @Operation
    public void createIndex(@Parameter String collection,
                            @Parameter String field,
                            @Parameter(optional = true, defaultValue = "ASC") IndexOrder order)
    {
        client.createIndex(collection, field, order);
    }
    
    /**
     * Drops an existing index
     *  
     * {@code <drop-index collection="myCollection" name="#[map-payload:anIndexName]"/>}
     * @param collection the name of the collection where the index is
     * @param index the name of the index to drop
     */
    @Operation
    public void dropIndex(@Parameter String collection, @Parameter String index)
    {
        client.dropIndex(collection, index);
    }
    
    /**
     * List existent indices in a collection
     * 
     * {@code <drop-index collection="myCollection" name="#[map-payload:anIndexName]"/>}
     * @param collection the name of the collection
     * @return a collection of DBObjects with indices information  
     */
    @Operation
    public Collection<DBObject> listIndices(@Parameter String collection)
    {
        return client.listIndices(collection);
    }

    public void initialise() throws InitialisationException
    {
        if (client == null)
        {
            try
            {
                client = new MongoClientImpl(getDatabase(getMongo()));
            }
            catch (Exception e)
            {
                throw new InitialisationException(e, this);
            }
        }
    }
    
    private synchronized Mongo getMongo() throws Exception
    {
        MongoRef ref = new MongoRef(host, port);
        Mongo mongo = MONGOS.get(ref);
        if (mongo == null)
        {
            mongo = newMongo();
            MONGOS.put(ref, mongo);
        }
        return mongo;
    }
    
    protected Mongo newMongo() throws UnknownHostException
    {
        return new Mongo(host, port);
    }

    private DB getDatabase(Mongo m)
    {
        DB db = m.getDB(database);
        if (password != null)
        {
            Validate.notNull(username, "Username must not be null if password is set");
            db.authenticate(username, password.toCharArray());
        }
        return db;
    }

    public MongoClient getClient()
    {
        return client;
    }

    public void setClient(MongoClient client)
    {
        this.client = client;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T single(Object o1, T o2 )
    {
        Validate.isTrue(o1 == null || o2 == null, "Can not specify both arguments at the same time" );
        return (T) (o1 != null ? o1 : o2); 
    }


    @SuppressWarnings("unused")
    protected static final class MongoRef
    {
        private final String host;
        private final int port;

        public MongoRef(String host, int port)
        {
            this.host = host;
            this.port = port;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }
    
    
}

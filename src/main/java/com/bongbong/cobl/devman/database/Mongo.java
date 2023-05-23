package com.bongbong.cobl.devman.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class Mongo {
    private final MongoDatabase mongoDatabase;

    /**
     * This method deletes a document in a specified {@link MongoCollection}
     *
     * @param collectionName The collection the document exists within
     * @param identifier The value for the default key "_id" (ex: uuid / uid)
     */
    public void deleteDocument(String collectionName, Object identifier) {
        deleteDocument(collectionName, "_id", identifier);
    }

    /**
     * This method deletes a document in a specified {@link MongoCollection}
     *
     * @param collectionName The collection the document exists within
     * @param key The document-key linked to the value (default: "_id")
     * @param value The value for the specified-key (ex: uuid / uid)
     */
    public void deleteDocument(String collectionName, String key, Object value) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = null;

        if (collection.find(Filters.eq(key, value)).iterator().hasNext())
            document = collection.find(Filters.eq(key, value)).first();


        if (document == null) return;

        collection.deleteMany(document);
    }

    /**
     * This method returns a {@link Document} in a specified {@link MongoCollection}
     *
     * @param collectionName The collection the document exists within
     * @param key The document-key linked to the value (default: "_id")
     * @param value The value for the specified-key (ex: uuid / uid)
     *
     * @return returns a {@link Document}
     */
    public Document getDocument(String collectionName, String key, Object value) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        if (collection.find(Filters.eq(key, value)).iterator().hasNext())
            return collection.find(Filters.eq(key, value)).first();

        return null;
    }

    /**
     * This method updates the Mongo database using a {@link MongoUpdate}
     *
     * @param mongoUpdate An instantiated {@link MongoUpdate} object
     */
    public void massUpdate(MongoUpdate mongoUpdate) {
        massUpdate(mongoUpdate.getCollectionName(), mongoUpdate.getId(), mongoUpdate.getUpdate());
    }

    private void massUpdate(String collectionName, Object id, Map<String, Object> updates) throws LinkageError {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        Document document = collection.find(new Document("_id", id)).first();
        if (document == null) collection.insertOne(new Document("_id", id));

        updates.forEach((key, value) -> collection.updateOne(Filters.eq("_id", id), Updates.set(key, value)));
    }

    /**
     * This method creates a named collection in Mongo.
     *
     * @param collectionName What you want to name your collection
     */
    public void createCollection(String collectionName) {
        AtomicBoolean exists = new AtomicBoolean(false);
        mongoDatabase.listCollectionNames().forEach(s -> {
            if (s.equals(collectionName)) exists.set(true);
        });

        if (!exists.get()) mongoDatabase.createCollection(collectionName);
    }

    /**
     * This method gets you an interable version of a Mongo collection
     *
     * @param collectionName The name of the collection you want to pull
     * @return An {@link FindIterable<Document>} format for a Mongo collection
     */
    public FindIterable<Document> getCollectionIterable(String collectionName) {
        return mongoDatabase.getCollection(collectionName).find();
    }
}

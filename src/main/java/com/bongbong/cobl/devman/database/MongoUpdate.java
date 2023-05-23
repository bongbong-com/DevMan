package com.bongbong.cobl.devman.database;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public @Data class MongoUpdate {
    private final String collectionName;
    private final Object id;
    private Map<String, Object> update = new HashMap<>();

    /**
     * Used for inputting data into Mongo. Data will be stored in this format.
     *
     * @param key Example: "name"
     * @param value Example: "Suzanne Crook"
     *
     * @return This object, used for chain-linking
     */
    public MongoUpdate put(String key, Object value) {
        update.put(key, value);
        return this;
    }
}

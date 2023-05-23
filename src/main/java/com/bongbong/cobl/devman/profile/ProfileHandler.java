package com.bongbong.cobl.devman.profile;

import com.bongbong.cobl.devman.database.Mongo;
import com.bongbong.cobl.devman.database.MongoUpdate;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RequiredArgsConstructor
public abstract class ProfileHandler {
    private final Mongo mongo;
    private final Map<UUID, Profile> profiles = new HashMap<>();

    /**
     * Creates a profile and stores it in cache.
     *
     * @param uuid The unique-identifier for the profile
     *
     * @return A new profile object
     */
    abstract Profile createProfile(UUID uuid);

    /**
     * Update a profile, this varies depending on the implementation
     *
     * @param profile The profile you wish to update
     */
    abstract void updateProfile(Profile profile);

    /**
     * Pull a profile from the database
     *
     * @param uuid The UUID of the profile you wish to pull.
     * @param cache Do you want to cache the profile in cache?
     *
     * @return The requested profile or null if it doesn't exist
     */
    @Nullable
    abstract Profile pullProfile(UUID uuid, boolean cache);

    /**
     * Push a profile to the database
     *
     * @param profile The profile you want to push to the database
     * @param uncache Do you want to uncache the profile from cache?
     */
    public void pushProfile(Profile profile, boolean uncache) {
        MongoUpdate mu = new MongoUpdate("profiles", profile.getUuid());
        mu.put("data", profile.serialize());

        mongo.massUpdate(mu);

        if (uncache) uncacheProfile(profile.getUuid());
    }

    /**
     * Get a profile from cache
     *
     * @param uuid The UUID associated with the profile
     *
     * @return The profile object (if present in cache) or null
     */
    @Nullable
    public Profile getProfile(UUID uuid) {
        return profiles.get(uuid);
    }

    /**
     * Cache a specified profile in cache
     *
     * @param profile The profile you wish to store in cache
     */
    public void cacheProfile(Profile profile) {
        profiles.put(profile.getUuid(), profile);
    }

    /**
     * Remove a specified profile from cache
     *
     * @param uuid The UUID of the profile you wish to remove from cache
     */
    public void uncacheProfile(UUID uuid) {
        profiles.remove(uuid);
    }

    /**
     * Check whether a profile is present in cache.
     *
     * @param uuid The UUID of the profile you want to check
     *
     * @return True (yes) or False (no) *duh*
     */
    public boolean isProfileCached(UUID uuid) {
        return profiles.containsKey(uuid);
    }

    /**
     * Run {@link ProfileHandler#updateProfile(Profile)} on all cached profiles
     */
    public void updateAllProfiles() {
        for (Profile profile : profiles.values()) updateProfile(profile);
    }
}

package com.bongbong.cobl.devman.profile;

import com.bongbong.cobl.devman.database.Mongo;
import com.bongbong.cobl.grpc.profile.ProfileReply;
import com.bongbong.cobl.grpc.profile.ProfileRequest;
import com.bongbong.cobl.grpc.profile.RequesterGrpc;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public abstract class GlobalProfileHandler extends ProfileHandler {
    private final RedisBungeeAPI redisBungeeAPI;
    private final HashMap<String, Channel> channels = new HashMap<>();
    public GlobalProfileHandler(Mongo mongo, RedisBungeeAPI redisBungeeAPI) {
        super(mongo);
        this.redisBungeeAPI = redisBungeeAPI;

        for (String id : redisBungeeAPI.getAllProxies())
            channels.put(id, Grpc.newChannelBuilder(id, InsecureChannelCredentials.create()).build());
    }

    @RequiredArgsConstructor
    static class ProfileRequesterImpl extends RequesterGrpc.RequesterImplBase {
        private final ProfileHandler handler;
        @Override
        public void getProfile(ProfileRequest req, StreamObserver<ProfileReply> responseObserver) {
            UUID uuid = UUID.fromString(req.getUuid());
            Profile profile = handler.getProfile(uuid);

            if (profile == null) {
                responseObserver.onError(new NullPointerException());
                return;
            }

            ProfileReply reply = ProfileReply.newBuilder().setJsonProfile(profile.serialize()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    /**
     * Fetch data from a profile accross all connected {@link RedisBungeeAPI} proxies.
     * This is useful for getting realtime, updated data for users connected
     * on another proxy.
     *
     * @param uuid The UUID for the profile you want to fetch
     * @param dataBaseSearch Do you want to include offline players in your fetch?
     *
     * @return JSON Data-payload for the user profile
     */
    private String fetchGlobalProfileJson(UUID uuid, boolean dataBaseSearch) {
        String id = redisBungeeAPI.getProxy(uuid);
        if (id == null) {
            if (!dataBaseSearch) return null;
            return pullProfile(uuid, false).serialize();
        }

        RequesterGrpc.RequesterBlockingStub blockingStub = RequesterGrpc.newBlockingStub(channels.get(id));

        ProfileRequest request = ProfileRequest.newBuilder()
                .setUuid(uuid.toString()).build();
        ProfileReply response;
        try {
            response = blockingStub.getProfile(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }

        String profileJson = response.getJsonProfile();
        System.out.println(profileJson);

        return profileJson;
    }
}

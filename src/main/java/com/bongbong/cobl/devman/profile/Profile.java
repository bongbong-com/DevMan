package com.bongbong.cobl.devman.profile;

import java.util.UUID;

public interface Profile {
    UUID getUuid();
    String serialize();
    void deserialize(String string);
}

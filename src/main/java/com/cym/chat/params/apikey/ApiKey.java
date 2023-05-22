package com.cym.chat.params.apikey;

import java.util.Objects;

public class ApiKey {

    private String key;
    private boolean available;

    public ApiKey(String key) {
        this.key = key;
        this.available = true;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiKey apiKey = (ApiKey) o;
        return available == apiKey.available && Objects.equals(key, apiKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, available);
    }
}

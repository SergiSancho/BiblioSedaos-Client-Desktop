package com.bibliosedaos.desktop.util;

public class JwtStore {
    private static final JwtStore INSTANCE = new JwtStore();

    private String token;
    private String userId;
    private int rol;
    private long expiry;

    private JwtStore() {}

    public static JwtStore getInstance() { return INSTANCE; }

    public synchronized void setToken(String token) { this.token = token; }
    public synchronized String getToken() { return token; }

    public synchronized void setUserId(String userId) { this.userId = userId; }
    public synchronized String getUserId() { return userId; }

    public synchronized void setRol(int rol) { this.rol = rol; }
    public synchronized int getRol() { return rol; }

    public synchronized void setExpiry(long expiry) { this.expiry = expiry; }
    public synchronized long getExpiry() { return expiry; }

    public synchronized void clear() {
        token = null; userId = null; rol = 0; expiry = 0;
    }
}


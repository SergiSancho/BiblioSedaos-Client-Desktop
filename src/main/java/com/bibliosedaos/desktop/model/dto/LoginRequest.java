package com.bibliosedaos.desktop.model.dto;

public class LoginRequest {
    private String nick;
    private String password;

    public LoginRequest() {}

    public LoginRequest(String nick, String password) {
        this.nick = nick;
        this.password = password;
    }

    public String getNick() { return nick; }
    public void setNick(String nick) { this.nick = nick; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}


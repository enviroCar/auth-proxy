package org.envirocar.auth.proxy.security;

public class AuthenticatedUser {
    
    private String name;
    
    public AuthenticatedUser() {
        // sigh
    }
    
    AuthenticatedUser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

}

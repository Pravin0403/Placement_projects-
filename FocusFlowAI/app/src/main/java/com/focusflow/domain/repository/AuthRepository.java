package com.focusflow.domain.repository;

public interface AuthRepository {

    boolean isSignedIn();

    void login(String email, String password) throws Exception;

    void register(String email, String password) throws Exception;

    void logout();
}

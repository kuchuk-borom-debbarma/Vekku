package dev.kbd.vekku_server.auth;

record UserInfoDTO(
    String id,
    String email,
    String firstName,
    String password
) {}

package dev.kbd.vekku_server.dtos;

public record UserInfo(
    String id,
    String email,
    String firstName,
    String lastName
) {}

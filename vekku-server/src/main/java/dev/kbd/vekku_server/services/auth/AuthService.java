package dev.kbd.vekku_server.services.auth;

public interface AuthService {
    /**
     * Create a user with the given information
     * @param email email of the user to login with
     * @param password password of the user to login with
     * @param firstName first name of the user
     * @param lastName last name of the user
     */
    void createUser(
        String email,
        String password,
        String firstName,
        String lastName
    );
    /**
     * Login as a user
     * @param email email of the user to login with
     * @param password password of the user to login with
     * @return LoginData object containing the user's session token
     */
    LoginData login(String email, String password);

    public record LoginData(String token, String refreshToken, long expires) {}
}

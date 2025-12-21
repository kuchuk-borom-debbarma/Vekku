package dev.kbd.vekku_server.auth;

interface IAuthService {
    /**
     * Start sign up process. This should send OTP to the email
     * @param email email of the user to login with
     * @param password password of the user to login with
     * @param firstName first name of the user
     * @param lastName last name of the user
     * @return Token that needs to be passed during verification
     */
    String startSignUp(
        String email,
        String password,
        String firstName,
        String lastName
    );
    /**
     * Verify sign up process. This should verify the OTP sent to the email
     * @param otp OTP sent to the email
     * @param token Token that was returned during sign up
     */
    void verifySignUp(String otp, String token);
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

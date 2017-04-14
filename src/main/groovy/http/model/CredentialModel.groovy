package http.model

import http.utils.EnvironmentEnum

/**
 * Credential model.
 */
class CredentialModel {

    /** Environment. */
    EnvironmentEnum env
    /** Username. */
    String username
    /** Password. */
    String password
    /** API version. */
    String apiVersion

    /**
     * Constructor.
     * @param env environment
     * @param username username
     * @param password password
     * @param apiVersion api version
     */
    CredentialModel(EnvironmentEnum env, String username, String password, String apiVersion) {
        this.env = env
        this.username = username
        this.password = password
        this.apiVersion = apiVersion
    }
}

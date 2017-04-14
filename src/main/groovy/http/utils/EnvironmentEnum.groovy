package http.utils

/**
 * Environment SFDC.
 */
enum EnvironmentEnum {

    /** Production. */
    PROD('https://login.salesforce.com'),
    /** Sandbox. */
    SANDBOX('https://test.salesforce.com')

    /** SFDC url. */
    String url

    /**
     * Constructor.
     * @param url url
     */
    EnvironmentEnum(String url) {
        this.url = url
    }
}

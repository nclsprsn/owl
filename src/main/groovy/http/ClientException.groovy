package http

/**
 * Client exception.
 */
class ClientException extends Exception {

    /**
     * Constructor
     * @param code code
     * @param msg message
     */
    ClientException(Integer code, String msg) {
        super("Status: ${code} / Message: ${msg}")
    }

}

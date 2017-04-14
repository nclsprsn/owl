package api

/**
 * SOAP Exception.
 */
class SoapException extends Exception {

    SoapException(Integer code, String msg) {
        super("Status: ${code} / Message: ${msg}")
    }
}

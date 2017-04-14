package http

import groovy.util.logging.Slf4j
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import http.model.CredentialModel

/**
 * HTTP Client.
 */
@Slf4j
abstract class Client {

    /** Session Id. */
    String sessionId
    /** Type server url. */
    String metaServerUrl
    /** Server url. */
    String serverUrl
    /** Credential. */
    protected CredentialModel cred

    /**
     * Constructor.
     * @param cred credential
     */
    Client(CredentialModel cred) {
        this.cred = cred
        login()
    }

    /**
     * POST Method.
     * @param payload payload
     * @param responseClosure responseClosure
     */
    abstract post(String payload, Closure responseClosure)

    /**
     * Login.
     */
    private login() {

        log.info "Logged as ${cred.username} at ${cred.env.url}"

        def http = new HTTPBuilder(cred.env.url)

        http.request(Method.POST, ContentType.XML) {
            uri.path = "/services/Soap/u/${cred.apiVersion}"
            headers.'SOAPAction' = '""'
            headers.'Content-Type' = 'text/xml'

            def builder = new StreamingMarkupBuilder()
            builder.encoding = 'UTF-8'
            def payload = builder.bind {
                mkp.xmlDeclaration()
                namespaces << [soapenv:'http://schemas.xmlsoap.org/soap/envelope/']

                soapenv.Envelope {
                    soapenv.Header {}
                    soapenv.Body {
                        namespaces << ['': 'urn:partner.soap.sforce.com']
                        login {
                            username cred.username
                            password cred.password
                        }
                    }
                }
            }
            body = XmlUtil.serialize(payload)

            response.success = { response, reader ->
                serverUrl = reader.Body.loginResponse.result.serverUrl
                metaServerUrl = reader.Body.loginResponse.result.metadataServerUrl
                sessionId = reader.Body.loginResponse.result.sessionId
            }

            response.failure = { response, reader ->
                throw new ClientException(response.status, reader.Body.Fault.faultstring.text())
            }

        }

        http.shutdown()
    }
}

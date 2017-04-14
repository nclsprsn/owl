package http

import groovy.util.logging.Slf4j
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import http.model.CredentialModel

/**
 * HTTP CorePartnerClient.
 */
@Slf4j
class CorePartnerClient extends Client {

    /**
     * Constructor.
     * @param cred credential
     */
    CorePartnerClient(CredentialModel cred) {
        super(cred)
    }

    /**
     * POST method.
     * @param payload payload
     * @param responseClosure responseClosure
     */
    def post(String payload, Closure responseClosure) {
        post(payload, ContentType.XML, responseClosure)
    }

    /**
     * POST method.
     * @param bodyPayload payload
     * @param contentType content type
     * @param responseClosure responseClosure
     */
    def post(String bodyPayload, Object contentType, Closure responseClosure) {

        def http = new HTTPBuilder(serverUrl)

        http.request(Method.POST, contentType) {
            uri.path = ''
            headers.'SOAPAction' = '""'
            headers.'Content-Type' = 'text/xml'

            def builder = new StreamingMarkupBuilder()
            builder.encoding = 'UTF-8'
            def payload = builder.bind {
                mkp.xmlDeclaration()
                namespaces << [soapenv:'http://schemas.xmlsoap.org/soap/envelope/',
                               xsi:'http://www.w3.org/2001/XMLSchema-instance',
                               urn: 'urn:partner.soap.sforce.com',
                               urn1: 'urn:sobject.partner.api.sforce.com']

                soapenv.Envelope {

                    namespaces << ['': 'urn:partner.soap.sforce.com']
                    soapenv.Header {
                        SessionHeader {
                            sessionId sessionId
                        }
                    }

                    soapenv.Body {

                        mkp.yieldUnescaped bodyPayload
                    }
                }

            }
            body = XmlUtil.serialize(payload)
            log.debug XmlUtil.serialize(payload)

            response.success = { resp, reader ->
                responseClosure(resp, reader)
            }

            response.failure = { resp, reader ->
                responseClosure(resp, reader)
            }

        }

        http.shutdown()
    }
}

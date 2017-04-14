package api

import groovy.util.logging.Slf4j
import groovy.xml.StreamingMarkupBuilder
import groovyx.net.http.ContentType
import http.CorePartnerClient
import http.model.CredentialModel
import api.model.Package

/**
 * Description client API.
 */
@Slf4j
class DescriptionClient {

    /** Client. */
    CorePartnerClient client

    /**
     * Constructor.
     * @param cred credentials
     */
    DescriptionClient(CredentialModel cred) {
        this.client = new CorePartnerClient(cred)
    }

    /**
     * DescriptionClient metadata.
     * @param pkg package
     * @return metadata descriptions
     */
    def describeMetadata(Package pkg) {
        def result

        log.info 'Describe metadata'

        def payload = new StreamingMarkupBuilder().bind {
            describeSObjects {
                pkg.types.each { type ->
                    if (type.name == 'CustomObject') {
                        type.members.each { member ->
                            if (member != '*') {
                                sObjectType(member)
                            }
                        }
                    }
                }
            }
        }

        client.post(payload.toString()) { response, reader ->
            if (response.status == 200) {
                result = reader.Body.describeSObjectsResponse.result
            } else {
                throw new SoapException(response.status, reader.Body.Fault.faultstring.text())
            }

        }
        result
    }

    /**
     * DescriptionClient global.
     * @return global metadata descriptions
     */
    def describeGlobal() {
        def result = []

        log.info 'Describe global'

        def payload = new StreamingMarkupBuilder().bind {
            describeGlobal {}
        }

        client.post(payload.toString(), ContentType.XML) { response, reader ->
            if (response.status == 200) {
                reader.Body.describeGlobalResponse.result.depthFirst().findAll { it.name() == 'sobjects' }.each {
                    result << it.name
                }
            } else {
                throw new SoapException(response.status, reader.Body.Fault.faultstring.text())
            }

        }
        result
    }
}

package api

import api.model.Package
import groovy.util.logging.Slf4j
import groovy.xml.StreamingMarkupBuilder
import http.MetadataClient
import http.model.CredentialModel
import sun.misc.BASE64Decoder
import zip.ZipUtils

/**
 * PackageClient API.
 */
@Slf4j
class PackageClient {

    /** SFDC client. */
    MetadataClient client
    /** SFDC async job is done. */
    private Boolean done = false
    /** SFDC async Id for pending request. */
    private String asyncProcessId = null
    /** One second in milliseconds. */
    private static final long ONE_SECOND = 1000
    /** Maximum number of attempts to retrieve the results. */
    private static final int MAX_NUM_POLL_REQUESTS = 50

    /**
     * Constructor.
     * @param cred credentials
     */
    PackageClient(CredentialModel cred) {
        client = new MetadataClient(cred)
    }

    /**
     * Query package
     * @param pkg package
     * @throws SoapException exception
     */
    private void queryPackage(Package pkg) throws SoapException {

        log.info 'Query package'

        def payload = new StreamingMarkupBuilder().bind {
            retrieve {
                request {
                    unpackaged {
                        pkg.types.each { type ->
                            types {
                                name(type.name)
                                type.members.each { member ->
                                    members(member)
                                }
                            }
                        }
                    }
                }
            }
        }

        client.post(payload.toString()) { response, reader ->
            if (response.status == 200) {
                done = reader.Body.retrieveResponse.result.done
                asyncProcessId = reader.Body.retrieveResponse.result.id
            } else {
                throw new SoapException(response.status, reader.Body.Fault.faultstring.text())
            }
        }
    }

    /**
     * Wait until the package is processed by SFDC.
     * @throws SoapException exception
     */
    private void waitUntilProcessed() throws SoapException {
        int poll = 0
        long waitTimeMilliSecs = ONE_SECOND

        def payload = new StreamingMarkupBuilder().bind {
            checkStatus {
                asyncProcessId(asyncProcessId)
            }
        }

        Boolean done = false
        while (!done) {
            log.info "Wait to retrieve package - ${asyncProcessId}"
            Thread.sleep(waitTimeMilliSecs)
            // Double the wait time for the next iteration
            waitTimeMilliSecs *= 2
            if (poll++ > MAX_NUM_POLL_REQUESTS) {
                throw new Exception("""
                                    Request timed out.  If this is a large set
                                    of metadata components, check that the time allowed
                                    by MAX_NUM_POLL_REQUESTS is sufficient.
                                    """)
            }
            client.post(payload.toString()) { response, reader ->
                if (response.status == 200) {
                    done = reader.Body.checkStatusResponse.result.done.text() == 'true' ? true : false
                } else {
                    throw new SoapException(response.status, reader.Body.Fault.faultstring.text())
                }
            }
        }
    }

    /**
     * Retrieve package.
     * @return base64 zip file
     * @throws SoapException exception
     */
    private String retrievePackage() throws SoapException {
        String zipString = null

        def payload = new StreamingMarkupBuilder().bind {
            checkRetrieveStatus {
                asyncProcessId(asyncProcessId)
            }
        }

        client.post(payload.toString()) { response, reader ->
            if (response.status == 200) {
                zipString = reader.Body.checkRetrieveStatusResponse.result.zipFile
            } else {
                throw new SoapException(response.status, reader.Body.Fault.faultstring.text())
            }
        }

        log.info "Package retrieved - ${asyncProcessId}"

        return zipString
    }

    /**
     * Retrieve package and store his content locally.
     * @param pkg package
     * @param dir output path
     */
    def retrieve(Package pkg, File dir) {
        if (dir == null || !dir.isDirectory()) {
            throw new Exception("Not a directory")
        }

        queryPackage(pkg)
        waitUntilProcessed()
        String zipString = retrievePackage()
        BASE64Decoder decoder = new BASE64Decoder()
        ZipUtils.unZip(dir, new ByteArrayInputStream(decoder.decodeBuffer(zipString)))
    }

    /**
     * Retrieve package and return his content in-memory.
     * @param pkg package
     * @return Buffered zip content
     */
    def retrieveBuffered(Package pkg) {
        queryPackage(pkg)
        waitUntilProcessed()
        String zipString = retrievePackage()
        BASE64Decoder decoder = new BASE64Decoder()
        ZipUtils.unZipBuffered(new ByteArrayInputStream(decoder.decodeBuffer(zipString)))
    }
}
package zip

import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Zip library.
 */
@Slf4j
class ZipUtils {

    /**
     * Unzip.
     * @param path unzip path
     * @param inputStream zip data
     */
    static unZip(File path, InputStream inputStream) {
        def zipStream = new ZipInputStream(inputStream)

        byte[] buffer = new byte[2048]

        try {
            ZipEntry entry
            while ((entry = zipStream.getNextEntry()) != null) {
                log.debug String.format("Read entry: %s", entry.getName())

                String outpath = path.getAbsolutePath() + "/" + entry.getName()
                log.debug outpath
                new File(outpath).parentFile.mkdirs()
                new File(outpath).createNewFile()
                FileOutputStream output = null
                try {
                    output = new FileOutputStream(outpath)
                    int len = 0
                    while ((len = zipStream.read(buffer)) > 0) {
                        println output.toString()
                        output.write(buffer, 0, len)
                    }
                } finally {
                    if (output != null) {
                        output.close()
                    }
                }
            }
        }
        finally {
            zipStream.close()
        }
    }

    /**
     * Unzip.
     * @param path unzip path
     * @param inputStream zip data
     */
    static Map<String, OutputStream> unZipBuffered(InputStream inputStream) {
        Map<String, GPathResult> res = new HashMap<String, GPathResult>()
        def zipStream = new ZipInputStream(inputStream)

        byte[] buffer = new byte[2048]

        try {
            ZipEntry entry
            while ((entry = zipStream.getNextEntry()) != null) {
                log.debug String.format("Read entry: %s", entry.getName())

                ByteArrayOutputStream output = null
                try {
                    output = new ByteArrayOutputStream()
                    int len = 0
                    while ((len = zipStream.read(buffer)) > 0) {
                        output.write(buffer, 0, len)
                    }
                } finally {
                    if (output != null) {
                        output.close()
                    }
                }

                res.put(entry.getName(), new XmlSlurper().parse(new ByteArrayInputStream(output.toByteArray())))
            }
        }
        finally {
            zipStream.close()
        }
        res
    }
}

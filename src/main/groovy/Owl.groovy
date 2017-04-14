import api.DescriptionClient
import api.PackageClient
import api.build.PackageBuilder
import api.model.Package
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import document.Table
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import groovyjarjarcommonscli.Option
import http.model.CredentialModel
import http.utils.EnvironmentEnum
import org.slf4j.LoggerFactory

/**
 * Owl.
 */
@Slf4j
class Owl {

    /**
     * Main.
     * @param args args
     */
    static void main(String... args) {

        def cli = new CliBuilder(usage: 'Owl [options] <arguments>',
                header: 'Options:',
                footer: '-' * 73)

        cli.with {
            d longOpt: 'describe', 'List of available metadata'
            u longOpt: 'username', 'Organization username', args: 1, required: true
            p longOpt: 'password', 'Organization password', args: 1, required: true
            v longOpt: 'version', 'API version (default: 38.0)', args: 1
            e longOpt: 'environment', 'Is production environment ? (default: false)'
            m longOpt: 'metadata', args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, 'Extract metadata description'
            o longOpt: 'output', args: 1, 'Output file for extraction'
            l longOpt: 'log-level', args: 1, 'Log level: TRACE, DEBUG, INFO (default), WARN, ERROR'
            h longOpt: 'help', 'Usage information'
        }

        def options

        if ('-h' in args || '--help' in args) {
            cli.usage()
            return
        } else {
            options = cli.parse(args)
            if (options == null) {
                return
            }
        }

        // Set log level
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        root.setLevel(options.l instanceof Boolean ? Level.INFO : Level.toLevel(options.l, Level.INFO))

        // Environment configuration
        def apiVersion = options.v ? options.v : '38.0'
        def environment = options.e ? EnvironmentEnum.PROD : EnvironmentEnum.SANDBOX
        CredentialModel credential = new CredentialModel(environment, (String)options.u, (String)options.p, apiVersion)

        // Object list
        if(options.d) {
            log.info 'Start to describe global...'
            DescriptionClient metadata = new DescriptionClient(credential)
            def metadataObjects = metadata.describeGlobal()
            for(int i = 0; i < metadataObjects.size(); i += 2) {
                def nextIndex = i + 1
                System.out.printf("%-40.40s  %-40.40s%n",
                        metadataObjects.get(i),
                        metadataObjects.size() > nextIndex ? metadataObjects.get(nextIndex) : '')
            }
            log.info 'Describe global finished.'
        }

        // Extract object metadata
        if (options.m && options.o) {

            log.info 'Start to extract object metadata...'
            // Output file
            def outputFile = options.o

            // Load config
            def config = new ConfigSlurper().parse(new File('config/config.groovy').toURI().toURL())

            // Construct package for querying
            Package pkg = PackageBuilder.packageObjects(options.ms)

            // Retrieve metadata package
            PackageClient packageClient = new PackageClient(credential)
            Map<String, GPathResult> metadataPackage = packageClient.retrieveBuffered(pkg)
            Map<String, GPathResult> metadataPackageObjects = new HashMap<String, GPathResult>()
            metadataPackage.each { key, file ->
                if (key.endsWith('.object')) {
                    metadataPackageObjects.put(new File(key).getName().replace('.object', ''), file)
                }
            }

            // Retrieve metadata description
            DescriptionClient descriptionClient = new DescriptionClient(credential)
            def metadataDescriptionObjects = descriptionClient.describeMetadata(pkg)

            // Save
            Table.saveObjects(metadataDescriptionObjects, metadataPackageObjects, config.getProperty('document'), outputFile)

            log.info 'Extract object metadata finished.'
        }

    }
}

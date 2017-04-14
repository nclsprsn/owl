package api.build

import api.model.Package
import api.model.Type
import groovy.util.logging.Slf4j

/**
 * Package builder.
 */
@Slf4j
class PackageBuilder {

    /**
     * Construct package for querying objects.
     * @param objects objects
     * @return package
     */
    static packageObjects(objects) {
        Package pkg = new Package()
        Type customObject = new Type('CustomObject')
        objects.each {
            customObject.members.push(it)
        }
        pkg.types.add(customObject)
        pkg
    }
}

package api.model

/**
 * Package type.
 */
class Type {

    /** Metadata name. */
    String name
    /** Metadata members. */
    List<String> members = new ArrayList<String>()

    /**
     * Constructor.
     * @param name name
     */
    Type(String name) {
        this.name = name
    }
}

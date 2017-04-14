package document

import groovy.util.logging.Slf4j
import org.apache.poi.hssf.usermodel.HSSFWorkbook

/**
 * Save table.
 */
@Slf4j
class Table {

    /**
     * Save object table.
     * @param metadataDescription metadata provided by the description client
     * @param metadataPackage metadata provided by the package client
     * @param columns columns
     * @param output output file
     */
    static saveObjects(metadataDescription, metadataPackage, columns, output) {

        log.info 'Generate object table'

        new HSSFWorkbook().with { workbook ->

            metadataDescription.each { object ->
                createSheet("${object.name}").with { sheet ->

                    def docColumns = columns.keySet()

                    // Header
                    createRow(0).with { row ->
                        docColumns.eachWithIndex { String column, int columnIndex ->
                            createCell(columnIndex).with { cell ->
                                setCellValue(columns.getProperty(column))
                            }
                        }
                    }

                    // Body
                    object.fields.eachWithIndex { field, fieldIndex ->
                        createRow(fieldIndex + 1).with { row ->

                            def desc = ''
                            if (metadataPackage.get(object.name.toString()) != null
                                    && metadataPackage.get(object.name.toString()).fields != null) {
                                def descField = metadataPackage.get(object.name.toString()).fields.find { fieldPkg ->
                                    fieldPkg.fullName == field.name.toString()
                                }

                                if (descField != null) {
                                    desc = descField.description.toString()
                                }
                            }

                            docColumns.eachWithIndex { String column, int columnIndex ->
                                createCell(columnIndex).with { cell ->
                                    if (column != 'description') {
                                        setCellValue("${field."$column"}")
                                    } else {
                                        setCellValue(desc)
                                    }
                                }
                            }

                        }
                    }
                }
            }

            new File(output).withOutputStream { os ->
                write(os)
            }
        }
    }
}

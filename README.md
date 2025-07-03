# Simple XML validator

Validate a bunch of XML files against a bunch of XSD schema files.

Put XML into directory `xml`, and XSD into directory `xsd`. IntelliJ IDEA project file is included, just run `XMLValidator.java`.

The schema files should import other XSD files with `<import schemaLocation="filename.xsd">` if necessary.

# Schema Derivation

## Background

As stated in the [explanation of the XSS vulnerability scan](usage-xss.md), the scan requires a schema file to be present.
This is not technically an XML Schema (XSD), but it serves a similar purpose: It describes the input data that an XSLT program
may be assumed to process. Since this input not only consists of the XML data, but also has to consider stylesheet parameters
and extension functions, a different format had to be used.

The schema format is likely to change in the near future (see [issue 9](https://github.com/x2vc/x2vc/issues/9)), so no
description of the format exists as of yet. The structure should be fairly obvious when looking at the [example
files](https://github.com/x2vc/x2vc/tree/main/examples/manual/with-schema) provided, but that's what a lot of projects say:
"Don't worry, it's self-explanatory."

## Workflow

For reasons that need to be explained in a separate document, it is not possible to determine the schema without human
assistance. When running `x2vc schema <file(s)>`, x2vc will attempt to generate a basic schema if none exists. After this initial
run, a developer who knows about the application and its data structures is required to refine the schema. This includes

 * removing branches that are technically possible but will never be occur in the real application,
 * adjusting data types and multiplicity values,
 * marking values as non-user-modifiable to reduce false positives and
 * providing discrete values that represent value restrictions or relevant values to be checked.

The generation process still contains a few [issues](https://github.com/x2vc/x2vc/issues), so be sure to check
the list before attempting to work with x2vc. This area is still very much work in progress, as is the documentation.

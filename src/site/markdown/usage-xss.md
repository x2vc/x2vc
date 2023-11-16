# XSS Vulnerability Scan

## Prerequisites

In order to perform an XSS vulnerability scan, x2vc requires a schema file for each XSLT file to be examined. You can read more
about the process of creating the schema file [here](usage-schema.html). If you just want to give x2vc a try, it is recommended
to download the [example files](https://github.com/x2vc/x2vc/tree/main/examples/manual/with-schema) that already come with a
schema file. If you [built x2vc from source](build.html), you will find these examples in your local working copy as well.

x2vc needs write access to the directory the XSLT and schema files reside in to create the report and log files.

## Execution

The [basic invocation](usage.html) pattern applies. Use the mode `xss` to limit x2vc to just perform an XSS vulnerability scan
without changing the schema file provided.

```
$ x2vc xss po_*.xslt
```

This will generate a report file [as described here](usage.html) for each XSLT file. The report contains a description of each
potential vulnerability identified; more information about the types of vulnerabilites will be added to this documentation later
on.

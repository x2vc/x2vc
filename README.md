# x2vc - XSLT XSS Vulnerability Checker

Copyright 2023 x2vc authors and contributors

x2vc is a tool to check XSLT programs (aka stylesheets) that transform XML data
into HTML output. It attempts to identify paths by which XML input data can be
used to perform Cross-Site Scripting (XSS) attacks.

## Status

Please note that this is a pre-release version that is not feature-complete an
can not yet be considered production-ready (if a tool such as this can ever
be). Exercise caution, make frequent backups and treat the results with more
caution than usual.

## Requirements

x2vc requires at least Java 21 installed (check using java -version on the
command line). All other required libraries are bundled in the distribution
archive.

## Installation

Unzip the distribution file in a location of your choice (no administrative
privileges required). For convenience, it is recommended to add the bin/
directory to your search path environment variable.

## Use

Using the command line, navigate to the folder that contains the XSLT programs.
Then execute

    x2vc <mode> <file(s)>

where <mode> can be one of
 * `schema` to attempt to generate or extend the schema file, then exit
 * `xss` to only perform an XSS check using the existing schema, then exit
 * `full`to generate/extend the schema, then perform the XSS check
It is possible to specify more than one file by listing individual files
or using wildcards.

For more advanced options, check the documentation at https://x2vc.org

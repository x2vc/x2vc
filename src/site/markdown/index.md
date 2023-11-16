# Overview

x2vc is a tool to check [XSLT programs](https://www.w3.org/TR/xslt/) (aka stylesheets) that transform XML data into
HTML output. It attempts to identify paths by which XML input data can be used to perform [Cross-Site Scripting
(XSS)](https://cwe.mitre.org/data/definitions/79.html) attacks.

## ...but why?

XSL and XPath have been available since 1999. They can be used to process a set of input data, most often represented
as an XML document, and produce output in the form of another XML document, a HTML document, plain text or, through
more recent additions, other formats. In this case, we're intested in the HTML part.

For a certain period of time, when frameworks for web development were still in their infancy and the general tendency
to shift entire user interfaces to web technologies was not yet clear, both XSLT and XPath were commonly used to
generate HTML documents. These could be exported reports, documents with specific layouts, or other stand-alone
documents to be viewed independently of the application as well as entire web-based user interfaces. However, it was
also quite common to integrate HTML display areas into an existing desktop application, for example to take advantage
of better design options or to compensate for weaknesses in the native desktop component libraries. It could be said
that a rendering engine that would otherwise be found in a web browser is used as a highly flexible user interface
component. It's these applications that x2vc focuses on - desktop applications that use HTML-based user interface
components and thus expose themselves to the risk of XSS attacks by using web technologies.

With today's knowledge and current technologies, applications would probably be developed according to more modern
procedures and, for example, use hardened libraries for web development that already take precautions against XSS and
other attacks. However, this procedure cannot be applied to the - often considered legacy - applications mentioned above:
Apart from technological considerations like the age and technical performance of the equipment that has to be supported,
it is often not possible to convert the application interface to new technologies on
a large scale for regulatory or business reasons. Re-writing the entire user interface might be too costly on its own,
or it might, by changing critical components of the system, trigger a re-certification process that might in itself be
as costly as the development effort itself. Nevertheless, security vulnerabilities in legacy systems - both in the
core product components and in customized installations - may be present and must be identified and addressed to
safeguard operations.

## Where to go from here

If you want to give x2vc a try, head on over to the [installation](installation.html) and [usage](usage.html) instructions.
A number of example files are available so that you don't have to bring your own.


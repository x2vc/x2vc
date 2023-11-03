<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="no"/>
<!-- DO NOT PRETTY PRINT - text output is pretty picky about whitespace! -->

<xsl:template match="attributionReport">
x2vc - XSLT XSS Vulnerability Checker
Copyright 2023 x2vc authors and contributors

This application makes use of the libraries and projects listed below, which
are published under the respective licenses. Please note that this list
includes all transitive dependencies. See the dependency overview published
on the x2vc project website for a more detailed report.

<xsl:for-each select="dependencies/dependency">
<xsl:value-of select="name"/> version <xsl:value-of select="version"/>
<xsl:if test="projectUrl">
  available at <xsl:value-of select="projectUrl"/>
</xsl:if>
<xsl:for-each select="licenses/license">
  License: <xsl:value-of select="name"/>
<xsl:if test="url">
    available at <xsl:value-of select="url"/>
</xsl:if>
</xsl:for-each>
<xsl:text>

</xsl:text>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>


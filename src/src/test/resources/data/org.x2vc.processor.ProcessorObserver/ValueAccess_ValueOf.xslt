<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="root">
		<html>
			<p><xsl:value-of select="@attrib"/></p>
			<p><xsl:value-of select="."/></p>
		</html>
	</xsl:template>
</xsl:stylesheet>
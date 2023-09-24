<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" />
	<xsl:template match="root">
		<html>
			<xsl:element name="body">
				<xsl:attribute name="foo">bar</xsl:attribute>
				<xsl:attribute name="{@attrib1}"><xsl:value-of select="." /></xsl:attribute>
			</xsl:element>
		</html>
	</xsl:template>
</xsl:stylesheet>
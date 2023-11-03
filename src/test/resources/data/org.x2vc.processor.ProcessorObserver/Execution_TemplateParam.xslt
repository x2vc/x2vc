<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<html>
			<xsl:call-template name="foobar">
				<xsl:with-param name="param1">actual1"</xsl:with-param>
			</xsl:call-template>
		</html>
	</xsl:template>
	<xsl:template name="foobar">
		<xsl:param name="param1" select="default1"/>
		<body>
			<xsl:value-of select="$param1" />
		</body>
	</xsl:template>
</xsl:stylesheet>
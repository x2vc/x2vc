<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes"/>
	<xsl:variable name="globalVar">globalValue</xsl:variable>
	<xsl:template match="root">
		<html>
			<xsl:call-template name="foobar" />
		</html>
	</xsl:template>
	<xsl:template name="foobar">
		<xsl:variable name="localVar" select="@attrib1"/>
		<body>
			<xsl:value-of select="$globalVar"/>
			<br/>
			<xsl:value-of select="$localVar"/>
		</body>
	</xsl:template>
</xsl:stylesheet>
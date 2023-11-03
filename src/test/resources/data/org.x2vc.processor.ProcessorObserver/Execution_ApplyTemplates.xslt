<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="root">
		<html>
			<body>
				<xsl:apply-templates select="elem"/>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="elem">
		<p>Some Content.</p>
	</xsl:template>
</xsl:stylesheet>
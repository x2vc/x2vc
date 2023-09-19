<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" />
	<xsl:template match="root">
		<html>
			<xsl:for-each select="elem[@attrib='foo']">
				<p>
					<xsl:value-of select="." />
				</p>
			</xsl:for-each>
			<xsl:for-each select="elem[@attrib='bar']">
				<div>
					<xsl:value-of select="." />
				</div>
			</xsl:for-each>
			<xsl:for-each select="elem[@attrib='baz']">
				<div>
					<xsl:value-of select="." />
				</div>
			</xsl:for-each>
		</html>
	</xsl:template>
</xsl:stylesheet>
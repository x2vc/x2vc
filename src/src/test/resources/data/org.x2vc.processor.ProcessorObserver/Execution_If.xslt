<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="root">
		<html>
			<xsl:if test="@attribA='foo'">
				A is foo.
			</xsl:if>
			<xsl:if test="@attribB='foo'">
				B is foo.
			</xsl:if>
		</html>
	</xsl:template>
</xsl:stylesheet>
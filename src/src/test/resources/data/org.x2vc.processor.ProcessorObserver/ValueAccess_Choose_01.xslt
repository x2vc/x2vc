<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" />
	<xsl:template match="root">
		<html>
			<xsl:choose>
				<xsl:when test="@value1='foo'">
					Value 1 is foo.
				</xsl:when>
				<xsl:when test="@value2='foo'">
					Value 2 is foo.
				</xsl:when>
				<xsl:otherwise>
					None of the values is foo.
				</xsl:otherwise>
			</xsl:choose>
		</html>
	</xsl:template>
</xsl:stylesheet>
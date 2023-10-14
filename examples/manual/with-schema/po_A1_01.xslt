<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<html>
			<xsl:apply-templates/>
		</html>
	</xsl:template>
	<xsl:template match="purchaseOrder">
		<head>
			<xsl:call-template name="poHeader"/>
		</head>
		<body>
			<xsl:call-template name="poBody"/>
		</body>
	</xsl:template>
	<xsl:template name="poHeader">
		<title>Purchase Order
			<xsl:value-of select="@orderNumber"/>
		</title>
		<xsl:choose>
			<xsl:when test="@system = 'dev'">
				<script src="dev/commonFunctions.js"></script>
				<link rel="stylesheet" href="dev/commonStyles.css" />
			</xsl:when>
			<xsl:when test="@system = 'test'">
				<script src="test/commonFunctions.js"></script>
				<link rel="stylesheet" href="test/commonStyles.css" />
			</xsl:when>
			<xsl:when test="@system = 'prod'">
				<script src="prod/commonFunctions.js"></script>
				<link rel="stylesheet" href="prod/commonStyles.css" />
			</xsl:when>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="@background = 'ivory'">
				<style>
					body {background-color: ivory;}
				</style>
			</xsl:when>
			<xsl:when test="@background = 'beige'">
				<style>
					body {background-color: beige;}
				</style>
			</xsl:when>
			<xsl:when test="@background = 'seashell'">
				<style>
					body {background-color: seashell;}
				</style>
			</xsl:when>
			<xsl:otherwise>
				<style>
					body {background-color: white;}
				</style>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="poBody">
		<h1>Purchase Order</h1>
		<table>
			<tr>
				<td valign="top">
					<xsl:call-template name="poData"/>
				</td>
				<td valign="top">
					<xsl:apply-templates select="shipTo"/>
				</td>
				<td valign="top">
					<xsl:apply-templates select="billTo"/>
				</td>
			</tr>
		</table>
		<xsl:apply-templates select="items"/>
	</xsl:template>
	<xsl:template name="poData">
		<xsl:comment>purchase order data</xsl:comment>
		<table>
			<tr>
				<th>Order Number:</th>
				<td>
					<xsl:value-of select="@orderNumber"/>
				</td>
			</tr>
			<tr>
				<th>Order Date:</th>
				<td>
					<xsl:value-of select="@orderDate"/>
				</td>
			</tr>
			<tr>
				<th>Comments:</th>
				<td>
					<xsl:apply-templates select="comment" mode="po"/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="shipTo">
		<xsl:comment>shipping information</xsl:comment>
		<div>
			<b>Ship To:</b>
		</div>
		<xsl:call-template name="address">
			<xsl:with-param name="use">ship</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="billTo">
		<xsl:comment>billing information</xsl:comment>
		<div>
			<b>Bill To:</b>
		</div>
		<xsl:call-template name="address">
			<xsl:with-param name="use">bill</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="address">
		<xsl:param name="use" />
		<div>
			<xsl:value-of select="name"/>
		</div>
		<div>
			<xsl:value-of select="street"/>
		</div>
		<div>
			<xsl:value-of select="city"/>
			<xsl:text>, </xsl:text>
			<xsl:value-of select="state"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="zip"/>
		</div>
		<div>
			<xsl:value-of select="@country"/>
		</div>
		<xsl:if test="phone">
			<div>
				Phone:
				<span id="{$use}phone"><xsl:value-of select="phone"/></span>
				<button onclick="window.open('tel:' + document.getElementById('{$use}phone').innerHTML);">call</button>
			</div>
		</xsl:if>
	</xsl:template>
	<xsl:template match="items">
		<xsl:comment>order items</xsl:comment>
		<table border="1">
			<tr>
				<th>Pos.</th>
				<th>Part No.</th>
				<th>Product</th>
				<th>Image</th>
				<th>Qty.</th>
				<th>Price</th>
				<th>Ship Date</th>
			</tr>
			<xsl:apply-templates select="item"/>
		</table>
	</xsl:template>
	<xsl:template match="item">
		<xsl:comment>item number
			<xsl:value-of select="position()"/>:
			<xsl:value-of select="product/productName"/>
		</xsl:comment>
		<tr>
			<td>
				<xsl:value-of select="position()"/>
			</td>
			<td>
				<xsl:value-of select="product/@partNum"/>
			</td>
			<td>
				<xsl:value-of select="product/productName"/>
			</td>
			<td>
				<xsl:if test="product/productImage">
					<xsl:element name="img">
						<xsl:attribute name="src">
							<xsl:value-of select="concat('https://my.cdn.com/products/', @partNum, '.png')" />
						</xsl:attribute>
						<!-- BAD EXAMPLE: this should trigger Rule A.1 because it allows for
						     insertion of an arbitrary attribute through a source attribute value -->
						<xsl:attribute name="{product/productImage/@textPlacement}">
							<xsl:value-of select="product/productImage/text" />
						</xsl:attribute>
					</xsl:element>
				</xsl:if>
			</td>
			<td>
				<xsl:value-of select="quantity"/>
			</td>
			<td>
				<xsl:value-of select="concat('$', USPrice)"/>
			</td>
			<td>
				<xsl:value-of select="shipDate"/>
			</td>
		</tr>
		<xsl:apply-templates select="comment" mode="item"/>
	</xsl:template>
	<xsl:template match="comment" mode="po">
		<div>
			<!-- exclude @* because we don't want to inject attributes into surrounding td -->
			<xsl:apply-templates select="text()|b|i|br|span" mode="xss-filter"/>
		</div>
	</xsl:template>
	<xsl:template match="comment" mode="item">
		<tr>
			<td/>
			<td colspan="5">
				<b>Comment:</b>
				<xsl:text> </xsl:text>
				<!-- exclude @* because we don't want to inject attributes into surrounding td -->
				<xsl:apply-templates select="text()|b|i|br|span" mode="xss-filter"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="@*|*" mode="xss-filter">
		<xsl:copy>
			<xsl:apply-templates select="@style|text()|b|i|br|span" mode="xss-filter"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
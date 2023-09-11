<?xml version="1.0"?>
<!DOCTYPE stylesheet [
  <!ENTITY % w3centities-f PUBLIC "-//W3C//ENTITIES Combined Set//EN//XML" "http://www.w3.org/2003/entities/2007/w3centities-f.ent">
  %w3centities-f;
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" />
	<xsl:template match="/vulnerabilityReport">
		<html>
			<xsl:call-template name="head" />
			<body class="is-preload" onload="PR.prettyPrint()">
				<xsl:call-template name="body-contents" />
			</body>
		</html>
	</xsl:template>

	<xsl:template name="head">
		<head>
			<title>x2vc Result | <xsl:value-of select="@stylesheetFilename"/></title>
			<meta charset="utf-8" />
			<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
			<link rel="stylesheet" type="text/css" href=".x2vc/css/main.css" />
			<link rel="stylesheet" type="text/css" href=".x2vc/css/prettify.css" />
			<script type="text/javascript" src=".x2vc/js/jquery.min.js"></script>
			<script type="text/javascript" src=".x2vc/js/browser.min.js"></script>
			<script type="text/javascript" src=".x2vc/js/breakpoints.min.js"></script>
			<script type="text/javascript" src=".x2vc/js/util.js"></script>
			<script type="text/javascript" src=".x2vc/js/main.js"></script>
			<script type="text/javascript" src=".x2vc/js/prettify.js"></script>
		</head>
	</xsl:template>

	<xsl:template name="header">
		<header id="header">
			<div class="logo">XSLT XSS Vulnerability Check by x2vc</div>
			<ul class="icons">
				<li>
					<a href="https://github.com/x2vc" class="icon brands fa-github">
						<span class="label">github</span>
					</a>
				</li>
			</ul>
		</header>
	</xsl:template>

	<xsl:template name="body-contents">
		<div id="wrapper">
			<div id="main">
				<div class="inner">
					<xsl:call-template name="header" />
					<section>
						<header class="main">
							<h1>Check Report</h1>
						</header>
						<xsl:call-template name="header-info" />
						<xsl:apply-templates select="sections/section" mode="content"/>
					</section>
				</div>
			</div>
			<xsl:call-template name="sidebar" />
		</div>
	</xsl:template>

	<xsl:template name="header-info">
		<p>
			The file
			<a href="{@stylesheetURI}">
				<xsl:value-of select="@stylesheetFilename" />
			</a>
			was last checked for Cross-Site Scripting (XSS) vulnerabilities on
			<xsl:value-of select="format-dateTime(@checkDate, '[Y]-[M,2]-[D,2] [H]:[m]:[s]')" />.
			<xsl:choose>
				<xsl:when test="@totalIssues=0">
					No issues were found.
				</xsl:when>
				<xsl:when test="@totalIssues=1">
					One issue was found.
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@totalIssues" /> issues were found.
				</xsl:otherwise>
			</xsl:choose>
		</p>
	</xsl:template>

	<xsl:template match="section" mode="content">
		<h2 id="{concat('s', (count(preceding-sibling::section) + 1))}">
			<xsl:value-of select="ruleID" />
			-
			<xsl:value-of select="heading" />
		</h2>
		<p>
			<xsl:copy-of select="introduction/node()" />
		</p>
		<xsl:if test="description">
			<p>
				<xsl:copy-of select="description/node()" />
			</p>
		</xsl:if>
		<xsl:if test="countermeasures">
			<p>
				<xsl:copy-of select="countermeasures/node()" />
			</p>
		</xsl:if>
		<xsl:apply-templates select="issues/issue" mode="content" />
	</xsl:template>

	<xsl:template match="issue" mode="content">
		<h3 id="{concat('i', (count(../../preceding-sibling::section) + 1), '-', (count(preceding-sibling::issue) + 1))}">
			Issue <xsl:value-of select="count(preceding-sibling::issue) + 1" />
		</h3>
		<p>
			<xsl:choose>
				<xsl:when test="count(affectingInputElements/element) = 1">
					The value of the output element
					<code>
						<xsl:value-of select="affectedOutputElement" />
					</code>
					can be manipulated through the input element
					<code>
						<xsl:value-of select="affectingInputElements/element" />
					</code>.
				</xsl:when>
				<xsl:otherwise>
					The value of the output element
					<code>
						<xsl:value-of select="affectedOutputElement" />
					</code>
					can be manipulated through the following input elements:
					<ul>
						<xsl:apply-templates select="affectingInputElements/element" mode="content" />
					</ul>
				</xsl:otherwise>
			</xsl:choose>
		</p>
		<xsl:apply-templates select="examples/example" mode="content" />
	</xsl:template>

	<xsl:template match="element" mode="content">
		<li><code><xsl:value-of select="."/></code></li>
	</xsl:template>

	<xsl:template match="example" mode="content">
		<h4>
			Example
			<xsl:value-of select="count(preceding-sibling::example) + 1" />
		</h4>
		<dl>
			<dt>XML Input:</dt>
			<dd>
				<pre>
					<code class="prettyprint">
						<xsl:value-of select="input" />
					</code>
				</pre>
			</dd>
			<dt>HTML Output:</dt>
			<dd>
				<pre>
					<code class="prettyprint">
						<xsl:value-of select="output" />
					</code>
				</pre>
			</dd>
		</dl>
	</xsl:template>

	<xsl:template name="sidebar">
		<div id="sidebar">
			<div class="inner">
				<nav id="menu">
					<header class="major">
						<h2>Contents</h2>
					</header>
					<ul>
						<xsl:apply-templates select="sections/section" mode="toc"/>
					</ul>
				</nav>
				<footer id="footer">
					<p class="copyright">
						Check performed using <a href="https://github.com/x2vc">x2vc</a>.
						Report design based on <a href="https://html5up.net/editorial">Editorial</a>
						template by <a href="https://html5up.net">HTML5 UP</a>.
					</p>
				</footer>
			</div>
		</div>
	</xsl:template>

	<xsl:template match="section" mode="toc">
		<li>
			<xsl:choose>
				<xsl:when test="issues/issue">
					<span class="opener">
						<xsl:value-of select="ruleID" />
						-
						<xsl:value-of select="shortHeading" />
					</span>
					<ul>
						<xsl:apply-templates select="issues/issue" mode="toc" />
					</ul>
				</xsl:when>
				<xsl:otherwise>
					<a href="{concat('#s', (count(preceding-sibling::section) + 1))}">
						<xsl:value-of select="ruleID" />
						-
						<xsl:value-of select="shortHeading" />
					</a>
				</xsl:otherwise>
			</xsl:choose>
		</li>
	</xsl:template>

	<xsl:template match="issue" mode="toc">
		<a href="{concat('#i', (count(../../preceding-sibling::section) + 1), '-', (count(preceding-sibling::issue) + 1))}">
			Issue
			<xsl:value-of select="count(preceding-sibling::issue) + 1" />
		</a>
	</xsl:template>

</xsl:stylesheet>
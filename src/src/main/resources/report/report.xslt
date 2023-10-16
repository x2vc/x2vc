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
				<script type="text/javascript" src=".x2vc/js/jquery.min.js"></script>
				<script type="text/javascript" src=".x2vc/js/browser.min.js"></script>
				<script type="text/javascript" src=".x2vc/js/breakpoints.min.js"></script>
				<script type="text/javascript" src=".x2vc/js/util.js"></script>
				<script type="text/javascript" src=".x2vc/js/main.js"></script>
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
			<link rel="stylesheet" type="text/css" href=".x2vc/css/x2vc.css" />
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
						<xsl:apply-templates select="codeCoverage"/>
						<xsl:apply-templates select="messages"/>
					</section>
				</div>
			</div>
			<xsl:call-template name="sidebar" />
		</div>
	</xsl:template>

	<xsl:template name="header-info">
		<h2 id="overview">Overview</h2>
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
				<xsl:otherwise>
					The following issues were found:
					<table>
						<thead>
							<tr>
								<th colspan="2">Vulnerability Class</th>
								<th>Issues</th>
							</tr>
						</thead>
						<tbody>
							<xsl:apply-templates select="sections/section" mode="overview" />
							<tr>
								<th colspan="2">Total</th>
								<th>
									<xsl:value-of select="@totalIssues" />
								</th>
							</tr>
						</tbody>
					</table>
				</xsl:otherwise>
			</xsl:choose>
		</p>
		<xsl:apply-templates select="coverage" />
	</xsl:template>

	<xsl:template match="section" mode="overview">
	<xsl:if test="count(issues/issue)>0">
		<tr>
			<td>
				<xsl:value-of select="ruleID" />
			</td>
			<td>
				<xsl:value-of select="heading" />
			</td>
			<td>
				<xsl:value-of select="count(issues/issue)" />
			</td>
		</tr>
	</xsl:if>
</xsl:template>

	<xsl:template match="coverage">
		<p>
			The following table gives an overview of the coverage achieved by the check. A more detailed
			view on the parts of the stylesheet source code that have been covered can be obtained from the
			<a href="#codeCoverage">code coverage section</a> at the end of the report.
		</p>
		<table>
			<thead>
				<tr>
					<th style="width:25%"></th>
					<th style="width:15%">fully covered</th>
					<th style="width:15%">partially covered</th>
					<th style="width:15%">not covered</th>
					<th style="width:15%">empty</th>
					<th style="width:15%">total</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<th>XSLT Directives</th>
					<td><xsl:value-of select="byDirective/fullCoverageCount"/></td>
					<td><xsl:value-of select="byDirective/partialCoverageCount"/></td>
					<td><xsl:value-of select="byDirective/noCoverageCount"/></td>
					<td>-</td>
					<td><xsl:value-of select="byDirective/totalCount"/></td>
				</tr>
				<tr>
					<th></th>
					<td><xsl:value-of select="format-number(byDirective/fullCoveragePercentage, '##0.00')"/> %</td>
					<td><xsl:value-of select="format-number(byDirective/partialCoveragePercentage, '##0.00')"/> %</td>
					<td><xsl:value-of select="format-number(byDirective/noCoveragePercentage, '##0.00')"/> %</td>
					<td>-</td>
					<td></td>
				</tr>
				<tr>
					<th>Lines of Stylesheet Code</th>
					<td><xsl:value-of select="byLine/fullCoverageCount"/></td>
					<td><xsl:value-of select="byLine/partialCoverageCount"/></td>
					<td><xsl:value-of select="byLine/noCoverageCount"/></td>
					<td><xsl:value-of select="byLine/emptyCount"/></td>
					<td><xsl:value-of select="byLine/totalCount"/></td>
				</tr>
				<tr>
					<th></th>
					<td><xsl:value-of select="format-number(byLine/fullCoveragePercentage, '##0.00')"/> %</td>
					<td><xsl:value-of select="format-number(byLine/partialCoveragePercentage, '##0.00')"/> %</td>
					<td><xsl:value-of select="format-number(byLine/emptyPercentage, '##0.00')"/> %</td>
					<td><xsl:value-of select="format-number(byLine/noCoveragePercentage, '##0.00')"/> %</td>
					<td></td>
				</tr>
			</tbody>
		</table>
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

	<xsl:template match="codeCoverage">
		<h2 id="codeCoverage">Stylesheet Code Coverage</h2>
		<pre>
			<code class="prettyprint">
				<xsl:apply-templates select="line"/>
			</code>
		</pre>
	</xsl:template>

	<xsl:template match="line">
		<span>
			<xsl:attribute name="class">
				<xsl:choose>
				<xsl:when test="@coverage='FULL'">coverage-full</xsl:when>
				<xsl:when test="@coverage='PARTIAL'">coverage-partial</xsl:when>
				<xsl:when test="@coverage='NONE'">coverage-none</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<xsl:value-of select="text()" />
		</span>
	</xsl:template>

	<xsl:template match="messages">
		<h2 id="logMessages">Analyzer Log Messages</h2>
		<table>
			<thead>
				<tr>
					<th>Level</th>
					<th>Thread</th>
					<th>Message</th>
				</tr>
			</thead>
			<tbody>
				<xsl:apply-templates select="message" />
			</tbody>
		</table>
	</xsl:template>

	<xsl:template match="message">
		<tr>
			<xsl:attribute name="class">
				<xsl:choose>
				<xsl:when test="@level='FATAL'">msg-fatal</xsl:when>
				<xsl:when test="@level='ERROR'">msg-error</xsl:when>
				<xsl:when test="@level='WARN'">msg-warn</xsl:when>
				<xsl:when test="@level='INFO'">msg-info</xsl:when>
				<xsl:when test="@level='DEBUG'">msg-debug</xsl:when>
				<xsl:when test="@level='TRACE'">msg-trace</xsl:when>
				</xsl:choose>
			</xsl:attribute>
			<td class="msg-level"><xsl:value-of select="@level" /></td>
			<td class="msg-thread"><xsl:value-of select="@thread" /></td>
			<td><xsl:value-of select="text()" /></td>
		</tr>
	</xsl:template>

	<xsl:template name="sidebar">
		<div id="sidebar">
			<div class="inner">
				<nav id="menu">
					<header class="major">
						<h2>Contents</h2>
					</header>
					<ul>
						<li>
							<a href="#overview">Overview</a>
						</li>
						<xsl:apply-templates select="sections/section" mode="toc"/>
						<li>
							<a href="#codeCoverage">Code Coverage</a>
						</li>
						<li>
							<a href="#logMessages">Log Messages</a>
						</li>
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
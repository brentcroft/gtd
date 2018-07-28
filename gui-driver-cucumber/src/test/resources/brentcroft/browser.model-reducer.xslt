<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:util="xalan://com.brentcroft.gtd.utilities.XmlUtils"
		xmlns:text="xalan://com.brentcroft.gtd.utilities.TextUtils"
		xmlns:names="xalan://com.brentcroft.gtd.utilities.NameUtils"
		xmlns:a="com.brentcroft.gtd.model"
		exclude-result-prefixes="util text">
	<!--
		importing base template
	-->
	<xsl:import href="default.model-reducer.xslt"/>
	<!--


	-->
	<xsl:template match="*[ @guid='browser' ]">
		<xsl:call-template name="makeModelElement"/>
	</xsl:template>
	<!--

		HTML components

	-->
	<xsl:template match="div | a | form | input | textarea | select | option">
		<xsl:call-template name="makeModelElement"/>
	</xsl:template>
	<!-- -->
	<xsl:template match="title | header | footer | section | article">
		<xsl:call-template name="makeModelElement"/>
	</xsl:template>
	<!-- -->
	<xsl:template match="ul | ol | table | tr | td | th">
		<xsl:call-template name="makeModelElement"/>
	</xsl:template>
</xsl:stylesheet>

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
	<xsl:import href="browser.model-reducer.xslt"/>	
	<!--

		suppressions
	-->
	<xsl:template match="div[ count( * ) = 0 ]"/>
	<!--

		elisions
	-->
	<xsl:template match="table | tr | td">
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>

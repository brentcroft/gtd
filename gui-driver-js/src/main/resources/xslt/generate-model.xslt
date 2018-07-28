<?xml version="1.0" encoding="UTF-8"?>
<!--
    generate a JSON object
-->
<xsl:stylesheet
		version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:text="xalan://com.brentcroft.util.TextUtils"
		xmlns:a="com.brentcroft.gtd.model">
	<xsl:output method="text" omit-xml-declaration="yes" indent="no" encoding="UTF-8" />
	<!--
        the adapter should provide a name
    -->
	<xsl:param name="model-name"/>
	<xsl:template match="*">{
		<xsl:if test="$model-name"></xsl:if>
		"$name": "<xsl:value-of select="$model-name"/>"<xsl:if test="*">,
			<xsl:call-template name="recurse"/></xsl:if>
		}
	</xsl:template>
	<!-- 	-->
	<xsl:template name="recurse">
		<xsl:choose>
			<xsl:when test="*[ @a:name and @a:xpath ]">
				<xsl:for-each select="*[ @a:name and @a:xpath ]">
					"<xsl:value-of select="normalize-space( @a:name )"/>" : {
					"$key" : "<xsl:value-of select="normalize-space( @a:key )"/>",
					"$actions" : "<xsl:value-of select="normalize-space( @a:actions )"/>",<xsl:if test="@on-input">
					"$onInput" : function(){<xsl:value-of select="normalize-space( @on-input )"/>;},</xsl:if>
					"$hash" : "<xsl:value-of select="normalize-space( @hash )"/>",
					"$xpath" : "<xsl:value-of select="text:replaceAll( normalize-space( @a:xpath ), '&quot;', '\\&quot;' )"/>"<xsl:if test="*[ @a:name and @a:xpath ]">,
					<xsl:call-template name="recurse"/></xsl:if>
					}<xsl:if test="position() != last()">, </xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="name" select="@a:name"/>
				<xsl:variable name="index" select="count( preceding-sibling::*[ name() = $name ] )"/>
				"<xsl:value-of select="normalize-space( @a:name )"/><xsl:if test="( $index &gt; 1 )">_<xsl:value-of select="$index"/></xsl:if>" : {
				"$key" : "<xsl:value-of select="normalize-space( @a:key )"/>",
				"$actions" : "<xsl:value-of select="normalize-space( @a:actions )"/>",<xsl:if test="@on-input">
				"$onInput" : function(){<xsl:value-of select="normalize-space( @on-input )"/>;},</xsl:if>
				"$hash" : "<xsl:value-of select="normalize-space( @hash )"/>",
				"$xpath" : "<xsl:value-of select="text:replaceAll( normalize-space( @a:xpath ), '&quot;', '\\&quot;' )"/>"
				}<xsl:if test="position() != last()">, </xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>

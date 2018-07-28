<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:a="com.brentcroft.gtd.model">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no" encoding="UTF-8" />
    <xsl:param name="root">this</xsl:param>
    <!--


    -->
    <xsl:template match="event-history">// generated
        <xsl:for-each select="dom-event[ model[ @name ] ] | fx-event[ model[ @name ] ] | awt-event[ model[ @name ] ]">
            <!--<xsl:sort select="@timestamp" data-type="text" order="ascending"/>-->
            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="parent" select="model/@parent"/>
            <xsl:variable name="name" select="model/@name"/>
            <xsl:variable name="action">
                <xsl:choose>
                    <xsl:when test="
						   ( @id = 'keydown' )
						or ( @id = 'keyup' )
						or ( @id = 'keypress' )
						or ( @id = 'mousedown' )
						or ( @id = 'mouseup' )
					"/>
                    <xsl:when test="( @id = 502 or @id = 500 ) and ( */model/@selected-index )">setIndex( <xsl:value-of select="*/model/@selected-index"/> )</xsl:when>
                    <xsl:when test="( @id = 'input' and select )">setIndex( &quot;<xsl:value-of select="select/@selected-index"/>&quot; )</xsl:when>
                    <xsl:when test="( @id = 500 or @id = 'click' ) and ( */*[ @a:actions and not( contains( @a:actions, 'robot' ) ) ] )">click()</xsl:when>
                    <xsl:when test="( @id = 402 ) or ( @id = 'KEY_TYPED' )">setText( &quot;<xsl:value-of select="*[ @hash ]/@text"/>&quot; )</xsl:when>
                    <xsl:when test="( @id = 'input' )">setText( &quot;<xsl:value-of select="*[ @hash ]/@value"/>&quot; )</xsl:when>
                    <xsl:otherwise>exists()</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="is-preceded" select="preceding-sibling::*[ model/@parent = $parent ]"/>
            <xsl:variable name="is-followed" select="following-sibling::*[ model/@parent = $parent ]"/>
            <xsl:variable name="p_seq" select="$is-preceded[ position() = 1 ]/@seq"/>
            <xsl:variable name="is-overridden" select="following-sibling::*[ model/@parent = $parent ][ $name = model/@name ]"/>
            <xsl:choose>
                <xsl:when test="string-length( $action ) &lt; 1"/>
                <xsl:when test="$is-overridden and not( $is-preceded ) and ( $is-followed )">
  // start common parent block [<xsl:value-of select="@seq"/>] id=<xsl:value-of select="@id"/>
  var p_<xsl:value-of select="@seq"/> = <xsl:value-of select="$root"/><xsl:value-of select="$parent"/>; 
                </xsl:when>				
                <xsl:when test="$is-overridden and not( $is-preceded )">
  // start common parent block [<xsl:value-of select="@seq"/>] id=<xsl:value-of select="@id"/>
  var p_<xsl:value-of select="@seq"/> = <xsl:value-of select="$root"/><xsl:value-of select="$parent"/>; 
  
  p_<xsl:value-of select="@seq"/>.<xsl:value-of select="concat( $name, '.', $action )"/>;  
                </xsl:when>
                <xsl:when test="$is-overridden"/>
                <xsl:when test="not( $is-preceded ) and not( $is-followed )"><xsl:if test="$parent">
  // isolated block [<xsl:value-of select="@seq"/>] id=<xsl:value-of select="@id"/><xsl:text>
  </xsl:text>
  <xsl:value-of select="$root"/><xsl:value-of select="$parent"/>.</xsl:if><xsl:value-of select="concat( $name, '.', $action )"/>;
				</xsl:when>
                <xsl:when test="not( $is-preceded ) and ( $is-followed )">
  // common parent block [<xsl:value-of select="@seq"/>] id=<xsl:value-of select="@id"/>
  var p_<xsl:value-of select="@seq"/> = <xsl:value-of select="$root"/><xsl:value-of select="$parent"/>; 

  p_<xsl:value-of select="@seq"/>.<xsl:value-of select="concat( $name, '.', $action )"/>;
                </xsl:when>
                <xsl:when test="( $is-preceded ) and not( $is-followed )">
  p_<xsl:value-of select="$p_seq"/>.<xsl:value-of select="concat( $name, '.', $action )"/>; 
  // end common parent block  [<xsl:value-of select="$p_seq"/>] id=<xsl:value-of select="@id"/><xsl:text>
  </xsl:text>
                </xsl:when>
                <xsl:otherwise>
  p_<xsl:value-of select="$p_seq"/>.<xsl:value-of select="concat( $name, '.', $action)"/>; // id=<xsl:value-of select="@id"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

    </xsl:template>
</xsl:stylesheet>
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

	-->
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="root-tag">reduction</xsl:param>
	<xsl:param name="text-translation-properties"/>
	<!--
		must contain in a top level element
	-->
	<xsl:template match="/">
		<xsl:element name="{ $root-tag }" xmlns:a="com.brentcroft.gtd.model">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<!--
		suppress any free text content and adapter elements
	-->
	<xsl:template match="@text"/>
	<xsl:template match="model"/>
	<!--

        suppress console window
		and a few others
	-->
	<xsl:template match="BorderPane[ TextArea[ @id = 'content-pane' ]/model[ starts-with( text(), 'Java Web Start' ) ] ]"/>
	<xsl:template match="BorderPane[ descendant::FlowPane[ @guid='console-dialog-button-bar' ] ]"/>
	<!--

		match any tabby elements
		NB: XSL 1.0 clumsy way to check if @actions endsWith 'tabs'
	-->
	<xsl:template match="*[ @tab-index or @tab-title or ( 'tabs' = substring( @actions, string-length( @actions ) - string-length( 'tabs' ) + 1 ) ) ]">
		<xsl:call-template name="makeModelElement"/>
	</xsl:template>
	<!--

	-->
	<!--

	-->
	<xsl:template match="
			JMenuBar |
			JMenu |
			JMenuItem |
			JPopupMenu |
			JPopupMenu-1 |
			JToggleButton |
			JButton |
			JCheckBox |
			JRadioButton |
			JRadioButtonMenuItem |
			JCheckBoxMenuItem |
			JSpinner |
			JToolBar |
			JComboBox |
			JList |
			JTabbedPane |
			JTable |
			JTree |
			JTextField |
			JTextPane |
			JTextArea |
			JFormattedTextField |
			JDialog |
			JFileChooser |
			JInternalFrame |
			JOptionPane
		">
		<xsl:call-template name="makeModelElement"/>
	</xsl:template>
	<!--

	-->
	<xsl:template match="
	    AnchorPane |
	    BorderPane |
        BreadcrumbBar |
        Button |
        ButtonBar |
        CheckBox |
	    GridPane |
        HTMLEditor |
        MenuBar |
        Menu |
        MenuItem |
        RadioButton |
        TabPane |
        Tab |
        TextArea |
        TextField |
        TitledPane |
		ToggleButton |
        TreeView |
		TreeItem |
		ToolBar |
        WebView
		">
		<xsl:call-template name="makeModelElement"/>
	</xsl:template>
	<!--

		template

	-->
	<xsl:template name="makeModelElement">
        <xsl:param name="tag" select="name()"/>
		<xsl:param name="name"/>
		<xsl:param name="xpath"/>
		<xsl:param name="actions"/>
		<xsl:param name="children"/>
		<xsl:variable name="inst-name" select="count( preceding-sibling::*[ @a:name = $name ] )"/>
		<xsl:element name="{ $tag }">
			<xsl:copy-of select="@*"/>
			<xsl:if test="$name"><xsl:attribute name="a:name"><xsl:value-of select="$name"/></xsl:attribute></xsl:if>
			<xsl:if test="$xpath"><xsl:attribute name="a:xpath"><xsl:value-of select="$xpath"/></xsl:attribute></xsl:if>
			<xsl:if test="$actions"><xsl:attribute name="actions"><xsl:value-of select="$actions"/></xsl:attribute></xsl:if>
			<xsl:choose>
				<xsl:when test="( $children = 'none' )"/>
				<xsl:when test="$children"><xsl:apply-templates select="$children"/></xsl:when>
				<xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>

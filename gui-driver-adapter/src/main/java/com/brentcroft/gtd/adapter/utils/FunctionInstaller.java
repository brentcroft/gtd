package com.brentcroft.gtd.adapter.utils;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class FunctionInstaller
{

	public class FunctionBuilder extends DefaultHandler
	{
		public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
		{
	        switch(qName)
	        {
	        	case "factory":
	        		
	        }


		}

		public void endElement( String uri, String localName, String qName ) throws SAXException
		{

		}

		public void characters( char ch[], int start, int length ) throws SAXException
		{
		}
	}

	
    public void newFunctionBuilder( File root, InputStream config )
    {
    	FunctionBuilder fb = new FunctionBuilder(  );
        try
        {
            SAXParserFactory
                    .newInstance()
                    .newSAXParser()
                    .parse( config, fb );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}

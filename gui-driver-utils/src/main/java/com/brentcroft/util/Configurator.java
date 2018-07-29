package com.brentcroft.util;

public interface Configurator< T, P >
{
    void configure( T t, P p );

    void export( T t, P p );

    class PropertiesConfigurator< T > implements Configurator< T, CommentedProperties >
    {
        private final TriConsumer< String, T, CommentedProperties > configurator;
        private final TriConsumer< String, T, CommentedProperties > exporter;

        private final String attribute;
        @SuppressWarnings( "unused" )
		private final String comment;

        public PropertiesConfigurator(
                String attribute,
                String comment,
                TriConsumer< String, T, CommentedProperties > configurator,
                TriConsumer< String, T, CommentedProperties > exporter )
        {
            this.configurator = configurator;
            this.exporter = exporter;
            this.attribute = attribute;
            this.comment = comment;
        }

        public void configure( T reducer, CommentedProperties p )
        {
            configurator.accept( attribute, reducer, p );
        }

        public void export( T reducer, CommentedProperties p )
        {
            exporter.accept( attribute, reducer, p );
        }
    }

    static < T > PropertiesConfigurator< T > create( String attribute,
                                                     String comment,
                                                     TriConsumer< String, T, CommentedProperties > configurator,
                                                     TriConsumer< String, T, CommentedProperties > exporter )
    {
        return new PropertiesConfigurator<>( attribute, comment, configurator, exporter );
    }
}

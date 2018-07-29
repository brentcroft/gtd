package com.brentcroft.gtd.inspector.model;

import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Window;

@SuppressWarnings( "restriction" )
public class ModelObjectDialog extends TextInputDialog
{
    public ModelObjectDialog withTitle( String title )
    {
        setTitle( title );
        return this;
    }

    public ModelObjectDialog withHeaderText( String message )
    {
        this.setHeaderText( message );
        return this;
    }

    public ModelObjectDialog withArgumentsText( String content )
    {
        this.setContentText( content );
        return this;
    }
    public ModelObjectDialog withOwner( Window owner )
    {
        this.initOwner( owner );
        return this;
    }

    public ModelObjectDialog withModality( Modality modality )
    {
        this.initModality(modality );
        return this;
    }    
}

/**
 * Backbone model denoting the remote Fuseki server.
 */
define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        sprintf = require( "sprintf" );

    /**
     * This model represents the users current choice of options to the
     * validation service.
     */
    var ValidationOptions = Backbone.Model.extend( {
      initialize: function() {
        this.set( {validateAs: "sparql"} );
        this.set( {outputFormat: "algebra"} );
      },

      validateAs: function() {
        return this.get( "validateAs" );
      },

      validateAsQuery: function() {
        return this.validateAs() === "sparql" || this.validateAs() === "arq";
      },

      setValidateAs: function( va ) {
        this.set( "validateAs", va );
        console.log( JSON.stringify( this.toJSON() ));
        console.log( "----" );
      },

      outputFormat: function() {
        return this.get( "outputFormat" );
      },

      setOutputFormat: function( of ) {
        this.set( "outputFormat", of );
      },

      toJSON: function() {
        var json = {
          languageSyntax: this.validateAs(),
          lineNumbers: true
        };

        if (this.validateAsQuery()) {
          json.outputFormat = this.outputFormat();
        }

        return json;
      }

    } );

    // when the models module starts, create the model
    fui.models.addInitializer( function( options ) {
      fui.models.validationOptions = new ValidationOptions();
      fui.vent.trigger( "models.validation-options.ready" );
    } );


    return ValidationOptions;
  }
);
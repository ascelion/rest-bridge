
package ascelion.rest.bridge.client;

import javax.ws.rs.HeaderParam;

class HeaderParamAction
extends AnnotationAction<HeaderParam>
{

	HeaderParamAction( HeaderParam annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		visitCollection( cx );
	}

	@Override
	void visitElement( RestContext cx, Object v )
	{
		cx.headers.add( this.annotation.value(), v );
	}
}

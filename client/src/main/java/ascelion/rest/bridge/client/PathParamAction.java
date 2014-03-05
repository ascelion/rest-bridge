
package ascelion.rest.bridge.client;

import javax.ws.rs.PathParam;

class PathParamAction
extends AnnotationAction<PathParam>
{

	PathParamAction( PathParam annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		cx.target = cx.target.resolveTemplate( this.annotation.value(), cx.parameterValue );
	}

}

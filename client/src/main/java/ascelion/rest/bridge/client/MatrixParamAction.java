package ascelion.rest.bridge.client;

import javax.ws.rs.MatrixParam;

class MatrixParamAction
extends AnnotationAction<MatrixParam>
{

	MatrixParamAction( MatrixParam annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
	}

}


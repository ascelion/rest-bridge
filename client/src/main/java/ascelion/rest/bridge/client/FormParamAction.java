
package ascelion.rest.bridge.client;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Form;

class FormParamAction
extends AnnotationAction<FormParam>
{

	FormParamAction( FormParam a, ActionParam p )
	{
		super( p, a );
	}

	@Override
	public void execute( RestRequest cx )
	{
		visitCollection( cx );
	}

	@Override
	void visitElement( RestRequest cx, Object v )
	{
		Form form = (Form) cx.entity;

		if( cx.entity == null ) {
			cx.entity = form = new Form();
		}

		form.param( this.annotation.value(), this.param.converter.apply( v ) );
	}

}

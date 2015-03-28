
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.BeanParam;

class BeanParamAction
extends AnnotationAction<BeanParam>
{

	BeanParamAction( BeanParam a, int ix )
	{
		super( a, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		if( cx.parameterValue != null ) {
			final Object bean = cx.parameterValue;

			for( final Field field : Util.getDeclaredFields( cx.parameterValue.getClass() ) ) {
				addAction( cx, field, bean );
			}
		}
	}

	private void addAction( RestContext cx, Field field, Object bean )
	{
		final Collection<Action> actions = new ArrayList<Action>();

		for( final Annotation a : field.getAnnotations() ) {
			final Action action = Action.createAction( a, 0 );

			if( action != null ) {
				actions.add( action );
			}
		}

		if( actions.size() > 0 ) {
			try {
				cx.parameterValue = field.get( bean );
			}
			catch( final IllegalAccessException e ) {
				throw new RuntimeException( e );
			}

			for( final Action action : actions ) {
				action.execute( cx );
			}
		}
	}
}

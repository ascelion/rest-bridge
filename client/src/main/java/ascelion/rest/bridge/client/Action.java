
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

abstract class Action
implements Comparable<Action>
{

	enum Priority
	{
		SET_VALUE,
		DEFAULT_VALUE,
		ANY,
	}

	static Action createAction( Annotation a, int ix )
	{
		if( CookieParam.class.isInstance( a ) ) {
			return new CookieParamAction( (CookieParam) a, ix );
		}
		if( DefaultValue.class.isInstance( a ) ) {
			return new DefaultValueAction( (DefaultValue) a, ix );
		}
		if( FormParam.class.isInstance( a ) ) {
			return new FormParamAction( (FormParam) a, ix );
		}
		if( MatrixParam.class.isInstance( a ) ) {
			return new MatrixParamAction( (MatrixParam) a, ix );
		}
		if( PathParam.class.isInstance( a ) ) {
			return new PathParamAction( (PathParam) a, ix );
		}
		if( QueryParam.class.isInstance( a ) ) {
			return new QueryParamAction( (QueryParam) a, ix );
		}
		if( HeaderParam.class.isInstance( a ) ) {
			return new HeaderParamAction( (HeaderParam) a, ix );
		}
		if( BeanParam.class.isInstance( a ) ) {
			return new BeanParamAction( (BeanParam) a, ix );
		}

		return null;
	}

	final int ix;

	final Action.Priority px;

	Action( int ix )
	{
		this.ix = ix;
		this.px = Priority.ANY;
	}

	Action( int ix, Action.Priority px )
	{
		this.ix = ix;
		this.px = px;
	}

	@Override
	public int compareTo( Action o )
	{
		if( this.ix != o.ix ) {
			return this.ix - o.ix;
		}

		return this.px.compareTo( o.px );
	}

	@Override
	public String toString()
	{
		return String.format( "%s=%d", getClass().getSimpleName(), this.ix );
	}

	void evaluate( Object[] arguments )
	{
	}

	abstract void execute( RestContext cx );
}

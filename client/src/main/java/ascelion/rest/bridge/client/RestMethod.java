
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;

import com.googlecode.gentyref.GenericTypeReflector;

class RestMethod
{

	static Collection collection( RestContext cx, Consumer action )
	{
		final Collection c;

		if( cx.parameterValue instanceof Collection ) {
			c = (Collection) cx.parameterValue;
		}
		else if( cx.parameterValue instanceof Object[] ) {
			c = Arrays.asList( (Object[]) cx.parameterValue );
		}
		else if( cx.parameterValue != null ) {
			c = Arrays.asList( cx.parameterValue );
		}
		else {
			c = Collections.EMPTY_LIST;
		}

		if( action != null ) {
			c.forEach( action );
		}

		return c;
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

	static <T extends Annotation> T getAnnotation( Class cls, Class<T> annCls )
	{
		T a = (T) cls.getAnnotation( annCls );

		if( a != null ) {
			return a;
		}

		for( final Class c : cls.getInterfaces() ) {
			a = getAnnotation( c, annCls );

			if( a != null ) {
				return a;
			}
		}

		return null;
	}

	//	static private final Logger L = LoggerFactory.getLogger( RestMethod.class );

	private final Class cls;

	private final Method method;

	private final Class returnType;

	private final String httpMethod;

	private final WebTarget target;

	private final Collection<Action> actions = new TreeSet<>();

	RestMethod( Class cls, Method method, WebTarget target )
	{
		this.cls = cls;
		this.method = method;
		this.returnType = method.getReturnType();
		this.httpMethod = Util.getHttpMethod( method );
		this.target = Util.addPathFromAnnotation( method, target );

		final Type exactReturnType = GenericTypeReflector.getExactReturnType( this.method, this.cls );

		if( this.httpMethod == null ) {
			if( this.target == target ) {
				throw new UnsupportedOperationException( "Not a resource method." );
			}

			this.actions.add( new SubresourceAction( (Class) exactReturnType ) );
		}
		else {
			final Class<?>[] types = method.getParameterTypes();

			for( int k = 0, z = types.length; k < z; k++ ) {
				this.actions.add( new SetValueAction( k ) );

				final Annotation[] pas = method.getParameterAnnotations()[k];
				boolean entityCandidate = true;

				for( final Annotation a : pas ) {
					final Action action = createAction( a, k );

					if( action != null ) {
						this.actions.add( action );

						entityCandidate = false;
					}
				}

				if( entityCandidate ) {
					final Type entityType = GenericTypeReflector.getExactParameterTypes( method, cls )[k];

					this.actions.add( new EntityAction( entityType, k ) );
				}
			}

			final Produces produces = getAnnotation( Produces.class );

			if( produces != null ) {
				this.actions.add( new ProducesAction( produces, this.actions.size() ) );
			}

			final Consumes consumes = getAnnotation( Consumes.class );
			if( consumes != null ) {
				this.actions.add( new ConsumesAction( consumes, this.actions.size() ) );
			}

			this.actions.add( new InvokeAction( this.actions.size(), this.httpMethod, exactReturnType ) );
		}
	}

	Object call( Object[] arguments, MultivaluedMap<String, Object> headers, Collection<Cookie> cookies, Form form )
	{
		final RestContext cx = new RestContext( this.target, headers, cookies, form );

		this.actions.forEach( a -> {
			a.evaluate( arguments );
			a.execute( cx );
		} );

		return cx.result;
	}

	private <T extends Annotation> T getAnnotation( Class<T> annCls )
	{
		final T annot = this.method.getAnnotation( annCls );

		if( annot != null ) {
			return annot;
		}

		return getAnnotation( this.cls, annCls );
	}
}

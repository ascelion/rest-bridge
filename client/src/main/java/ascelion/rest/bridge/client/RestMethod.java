
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
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

	static boolean isValidation( Annotation a )
	{
		return "javax.validation.Valid".equals( a.annotationType().getName() );
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
					final Action action = Action.createAction( a, k );

					if( action != null ) {
						this.actions.add( action );

						// TODO: fix this dirty hack
						if( !"ValidationAction".equals( action.getClass().getSimpleName() ) ) {
							entityCandidate = false;
						}
					}
				}

				if( entityCandidate ) {
					final Type entityType = GenericTypeReflector.getExactParameterTypes( method, cls )[k];

					this.actions.add( new EntityAction( entityType, k ) );
				}
			}

			this.actions.add( new ValidationAction( this.actions.size() ) );

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

	Object call( Object proxy, Object[] arguments, MultivaluedMap<String, Object> headers, Collection<Cookie> cookies, Form form )
	{
		final RestContext cx = new RestContext( proxy, this.method, arguments, this.target, headers, cookies, form );

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

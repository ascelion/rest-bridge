
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.WebTarget;

import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;

import io.leangen.geantyref.GenericTypeReflector;

final class RestMethod
{

	static <T extends Annotation> T getAnnotation( Class<?> cls, Class<T> annCls )
	{
		T a = cls.getAnnotation( annCls );

		if( a != null ) {
			return a;
		}

		for( final Class<?> c : cls.getInterfaces() ) {
			a = getAnnotation( c, annCls );

			if( a != null ) {
				return a;
			}
		}

		return null;
	}

	//	static private final Logger L = LoggerFactory.getLogger( RestMethod.class );

	private final Class<?> cls;
	final Method method;
	final WebTarget target;
	private final String httpMethod;
	private final List<Action> actions = new ArrayList<>( 8 );

	RestMethod( ConvertersFactory cvsf, Class<?> cls, Method method, WebTarget target )
	{
		this.cls = cls;
		this.method = method;
		this.httpMethod = Util.getHttpMethod( method );
		this.target = Util.addPathFromAnnotation( method, target );

		final Type exactReturnType = GenericTypeReflector.getExactReturnType( this.method, this.cls );

		if( this.httpMethod == null ) {
//			// TODO better check for resource methods
			if( this.target == target ) {
				throw new UnsupportedOperationException( "Not a resource method: " + method );
			}

			this.actions.add( new SubresourceAction( (Class) exactReturnType, new ActionParam( -1 ) ) );
		}
		else {
			final Parameter[] params = method.getParameters();

			final boolean hasEntity = false;

			this.actions.add( new ValidationAction( method ) );

			for( int q = 0, z = params.length; q < z; q++ ) {
				final int index = q;
				final Annotation[] annotations = params[index].getAnnotations();
				final Function<Object, String> cvt = cvsf.getConverter( params[index].getType(), annotations );
				final ActionParam p = new ActionParam( index, params[index].getType(), annotations, req -> req.arguments[index], cvt );
				final boolean entityCandidate = collectActions( annotations, p );

				if( entityCandidate ) {
					if( hasEntity ) {
						// TODO
						throw new RuntimeException( "already has entity" );
					}

					final Type entityType = GenericTypeReflector.getExactParameterTypes( method, cls )[index];

					this.actions.add( new EntityAction( p, entityType ) );
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

			this.actions.add( new InvokeAction( this.httpMethod, exactReturnType ) );
		}

		Collections.sort( this.actions );
	}

	private boolean collectActions( Annotation[] annotations, ActionParam p )
	{
		boolean entityCandidate = true;

		for( final Annotation a : annotations ) {
			if( !collectActions( a, p ) ) {
				entityCandidate = false;
			}
		}

		return entityCandidate;
	}

	private boolean collectActions( Annotation a, ActionParam p )
	{
		if( CookieParam.class.isInstance( a ) ) {
			this.actions.add( new CookieParamAction( (CookieParam) a, p ) );

			return false;
		}
		if( FormParam.class.isInstance( a ) ) {
			this.actions.add( new FormParamAction( (FormParam) a, p ) );

			return false;
		}
		if( MatrixParam.class.isInstance( a ) ) {
			this.actions.add( new MatrixParamAction( (MatrixParam) a, p ) );

			return false;
		}
		if( PathParam.class.isInstance( a ) ) {
			this.actions.add( new PathParamAction( (PathParam) a, p ) );

			return false;
		}
		if( QueryParam.class.isInstance( a ) ) {
			this.actions.add( new QueryParamAction( (QueryParam) a, p ) );

			return false;
		}
		if( HeaderParam.class.isInstance( a ) ) {
			this.actions.add( new HeaderParamAction( (HeaderParam) a, p ) );

			return false;
		}
		if( BeanParam.class.isInstance( a ) ) {
			final Iterable<Field> fields = getAllFieldsList( p.type );

			for( final Field field : fields ) {
				field.setAccessible( true );

				final Function<RestRequest, Object> sup = req -> {
					try {
						return field.get( p.currentValue( req ) );
					}
					catch( IllegalArgumentException | IllegalAccessException e ) {
						// TODO
						throw new RuntimeException( e );
					}
				};

				collectActions( field.getAnnotations(), new ActionParam( p.index, field.getType(), field.getAnnotations(), sup, p.converter ) );
			}

			return false;
		}

		return true;
	}

	void call( RestRequest req )
	{
		for( final Action a : this.actions ) {
			a.execute( req );
		}
	}

	private <T extends Annotation> T getAnnotation( Class<T> type )
	{
		final T annot = this.method.getAnnotation( type );

		if( annot != null ) {
			return annot;
		}

		return this.cls.getAnnotation( type );
	}
}

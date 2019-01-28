
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.ws.rs.*;

import ascelion.utils.etc.PropDescriptor;
import ascelion.utils.etc.TypeDescriptor;

import static ascelion.rest.bridge.client.RBUtils.findAnnotation;
import static ascelion.rest.bridge.client.RBUtils.pathParameters;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

final class DefaultRRIFactory implements RestRequestInterceptor.Factory
{

	static class Interceptors implements Iterable<RestRequestInterceptor>
	{

		private final Class<?> type;
		private final Method method;
		private final ConvertersFactory cvsf;

		private final Collection<RestRequestInterceptor> chain = new ArrayList<>();
		private final Map<String, Boolean> missingPaths;
		private RestParam entityParam;

		Interceptors( RestMethodInfo rmi )
		{
			this.type = rmi.getServiceType();
			this.method = rmi.getJavaMethod();
			this.cvsf = rmi.getConvertersFactory();

			this.missingPaths = pathParameters( rmi.getMethodURI() )
				.stream().collect( toMap( x -> x, x -> true ) );

			stream( this.method.getParameters() )
				.flatMap( p -> stream( p.getAnnotations() ) )
				.map( Annotation::annotationType )
				.filter( a -> isConstraint( a ) )
				.findAny()
				.ifPresent( p -> this.chain.add( new INTRequestValidation() ) );
			;

			ofNullable( this.method.getAnnotation( Valid.class ) )
				.ifPresent( a -> this.chain.add( new INTResponseValidation() ) );
			findAnnotation( Produces.class, this.method, this.type )
				.ifPresent( a -> this.chain.add( new INTProduces( a ) ) );
			findAnnotation( Consumes.class, this.method, this.type )
				.ifPresent( a -> this.chain.add( new INTConsumes( a ) ) );

			final Parameter[] params = this.method.getParameters();

			for( int q = 0, z = params.length; q < z; q++ ) {
				final int index = q;
				final Annotation[] annotations = params[index].getAnnotations();
				final LazyParamConverter<?> cvt = rmi.getConvertersFactory().getConverter( params[index].getType(), annotations );
				final RestParam p = new RestParam( index, params[index].getType(), cvt, params[index].getAnnotation( DefaultValue.class ), rc -> rc.getArgumentAt( index ) );

				if( createInterceptors( annotations, p ) ) {
					if( this.entityParam != null ) {
						throw new RestClientMethodException( format( "%s.%s: an entity is already present at parameter %d", this.type.getSimpleName(), this.method.getName(), this.entityParam.index ), this.method );
					}

					this.entityParam = p;

					this.chain.add( new INTEntity( p ) );
				}
			}

			final String missing = this.missingPaths.entrySet().stream()
				.filter( Map.Entry::getValue )
				.map( Map.Entry::getKey )
				.collect( joining( "}, {" ) );

			if( missing.length() > 0 ) {
				throw new RestClientMethodException( format( "Missing @PathParam for {%s}", missing ), this.method );
			}

			this.chain.add( INTSetMethod.INSTANCE );

			if( rmi.isAsync() ) {
				this.chain.add( new INTAsync() );
			}
		}

		@Override
		public Iterator<RestRequestInterceptor> iterator()
		{
			return this.chain.iterator();
		}

		private boolean createInterceptors( Annotation[] annotations, RestParam p )
		{
			boolean entityCandidate = true;

			for( final Annotation a : annotations ) {
				if( !createInterceptors( a, p ) ) {
					entityCandidate = false;
				}
			}

			return entityCandidate;
		}

		private boolean createInterceptors( Annotation annotation, RestParam param )
		{
			if( CookieParam.class.isInstance( annotation ) ) {
				this.chain.add( new INTCookieParam( (CookieParam) annotation, param ) );

				return false;
			}
			if( FormParam.class.isInstance( annotation ) ) {
				this.chain.add( new INTFormParam( (FormParam) annotation, param ) );

				return false;
			}
			if( MatrixParam.class.isInstance( annotation ) ) {
				this.chain.add( new INTMatrixParam( (MatrixParam) annotation, param ) );

				return false;
			}
			if( PathParam.class.isInstance( annotation ) ) {
				final PathParam v = (PathParam) annotation;

				if( this.missingPaths.containsKey( v.value() ) ) {
					this.missingPaths.put( v.value(), false );
				}
				else {
					throw new RestClientMethodException( format( "Unknown path element %s", v.value() ), this.method );
				}

				this.chain.add( new INTPathParam( v, param ) );

				return false;
			}
			if( QueryParam.class.isInstance( annotation ) ) {
				this.chain.add( new INTQueryParam( (QueryParam) annotation, param ) );

				return false;
			}
			if( HeaderParam.class.isInstance( annotation ) ) {
				this.chain.add( new INTHeaderParam( (HeaderParam) annotation, param ) );

				return false;
			}
			if( BeanParam.class.isInstance( annotation ) ) {
				final TypeDescriptor desc = new TypeDescriptor( param.type );
				boolean entityCandidate = true;

				for( final PropDescriptor<?> p : desc.getProperties() ) {
					if( !p.isReadable() || p.getDeclaringClass() == Object.class ) {
						continue;
					}

					final Function<RestRequestContext, Object> arg = rc -> p.get( param.argument.apply( rc ) );
					final LazyParamConverter<?> cvt = this.cvsf.getConverter( p.getType(), p.getAnnotations() );
					final RestParam par = new RestParam( param.index, p.getType(), cvt, p.getAnnotation( DefaultValue.class ), arg );

					if( !createInterceptors( p.getAnnotations(), par ) ) {
						entityCandidate = false;
					}
				}

				return entityCandidate;
			}

			return true;
		}

	}

	@Override
	public Iterable<RestRequestInterceptor> create( RestMethodInfo mi )
	{
		return new Interceptors( mi );
	}

	static private boolean isConstraint( Class<? extends Annotation> t )
	{
		return t == Valid.class || t.isAnnotationPresent( Constraint.class );
	}
}

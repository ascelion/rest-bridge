
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;

import com.googlecode.gentyref.GenericTypeReflector;

class RestMethod
{

	interface Action
	{

		void prepare( RestContext cx, Object[] arguments );
	}

	static abstract class AnnotationAction<A extends Annotation>
	implements Action, Comparable<AnnotationAction<Annotation>>
	{

		final A annotation;

		final int ix;

		private final Priority px;

		AnnotationAction( A annotation, int ix )
		{
			this( annotation, ix, Priority.NORMAL );
		}

		AnnotationAction( A annotation, int ix, Priority px )
		{
			this.annotation = annotation;
			this.ix = ix;
			this.px = px;
		}

		@Override
		public int compareTo( AnnotationAction<Annotation> o )
		{
			if( this.ix != o.ix ) {
				return this.ix - o.ix;
			}

			final int cp = -this.px.compareTo( o.px );

			if( cp != 0 ) {
				return cp;
			}

			if( this.annotation == null ) {
				return o.annotation == null ? 0 : +1;
			}
			if( o.annotation == null ) {
				return -1;
			}

			return this.annotation.getClass().getName().compareTo( o.annotation.getClass().getSimpleName() );
		}

		@Override
		public String toString()
		{
			final StringBuilder builder = new StringBuilder();

			builder.append( getClass().getSimpleName() );
			builder.append( "[ix=" );
			builder.append( this.ix );
			builder.append( ", px=" );
			builder.append( this.px );
			builder.append( ", annotation=" );
			builder.append( this.annotation );
			builder.append( "]" );

			return builder.toString();
		}

		Collection toCollection( RestContext cx )
		{
			if( cx.value instanceof Collection ) {
				return (Collection) cx.value;
			}
			else if( cx.value instanceof Object[] ) {
				return Arrays.asList( (Object[]) cx.value );
			}
			else if( cx.value != null ) {
				return Arrays.asList( cx.value );
			}
			else {
				return Collections.EMPTY_LIST;
			}
		}
	}

	static class ConsumesAction
	extends AnnotationAction<Consumes>
	{

		ConsumesAction( Consumes annotation )
		{
			super( annotation, Integer.MAX_VALUE - 2 );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
		}
	}

	static class CookieParamAction
	extends AnnotationAction<CookieParam>
	{

		CookieParamAction( CookieParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
			toCollection( cx ).forEach( v -> {
				if( v instanceof Cookie ) {
					final Cookie c = (Cookie) v;

					cx.cookies.add( new Cookie( this.annotation.value(),
						c.getValue(), c.getPath(), c.getDomain(), c.getVersion() ) );
				}
				else {
					cx.cookies.add( new Cookie( this.annotation.value(), v.toString() ) );
				}
			} );
		}

	}

	class DefaultValueAction
	extends AnnotationAction<DefaultValue>
	{

		public DefaultValueAction( DefaultValue annotation, int ix )
		{
			super( annotation, ix, Priority.HIGH );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
			if( cx.value == null ) {
				cx.value = this.annotation.value();
			}
		}

	}

	static class EntityAction
	extends AnnotationAction<Annotation>
	{

		private final Type entityType;

		private final int ix;

		EntityAction( Type entityType, int ix )
		{
			super( null, Integer.MAX_VALUE );

			this.entityType = entityType;
			this.ix = ix;
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
		}

	}

	static class FormParamAction
	extends AnnotationAction<FormParam>
	{

		FormParamAction( FormParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
			toCollection( cx ).forEach( v -> cx.form.param( this.annotation.value(), v.toString() ) );
		}

	}

	static class HeaderParamAction
	extends AnnotationAction<HeaderParam>
	{

		HeaderParamAction( HeaderParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
			toCollection( cx ).forEach( v -> cx.headers.add( this.annotation.value(), v ) );
		}

	}

	static class MatrixParamAction
	extends AnnotationAction<MatrixParam>
	{

		MatrixParamAction( MatrixParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
		}

	}

	static class PathParamAction
	extends AnnotationAction<PathParam>
	{

		PathParamAction( PathParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
			cx.target = cx.target.resolveTemplate( this.annotation.value(), toCollection( cx ) );
		}

	}

	enum Priority
	{
		LOW,
		NORMAL,
		HIGH,
		HIGHEST,
	}

	static class ProducesAction
	extends AnnotationAction<Produces>
	{

		ProducesAction( Produces annotation )
		{
			super( annotation, Integer.MAX_VALUE - 1 );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
		}

	}

	static class QueryParamAction
	extends AnnotationAction<QueryParam>
	{

		QueryParamAction( QueryParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void prepare( RestContext cx, Object[] unused )
		{
			cx.target = cx.target.queryParam( this.annotation.value(), toCollection( cx ) );
		}
	}

	class SetValueAction
	extends AnnotationAction<Annotation>
	{

		SetValueAction( int ix )
		{
			super( null, ix, Priority.HIGHEST );
		}

		@Override
		public void prepare( RestContext cx, Object[] arguments )
		{
			cx.value = arguments[this.ix];
		}
	}

	static WebTarget addPathFromAnnotation( final AnnotatedElement ae, WebTarget target )
	{
		final Path p = ae.getAnnotation( Path.class );
		if( p != null ) {
			target = target.path( p.value() );
		}
		return target;
	}

	static String getHttpMethod( Method method )
	{
		String httpMethod = getHttpMethodName( method );

		if( httpMethod != null ) {
			return httpMethod;
		}

		for( final Annotation ann : method.getAnnotations() ) {
			httpMethod = getHttpMethodName( ann.annotationType() );

			if( httpMethod != null ) {
				return httpMethod;
			}
		}

		return null;
	}

	static String getHttpMethodName( AnnotatedElement element )
	{
		final HttpMethod annotation = element.getAnnotation( HttpMethod.class );

		if( annotation != null ) {
			return annotation.value();
		}

		return null;
	}

	static private Class getReturnType( Class cls, Method method )
	{
		final Type returnType = GenericTypeReflector.getExactReturnType( method, cls );

		if( returnType instanceof Class ) {
			return (Class) returnType;
		}

		throw new UnsupportedOperationException( "Return type is not a class: " + returnType );
	}

	private final Class cls;

	private final Method method;

	private int entityIndex;

	private final Class returnType;

	private final String httpMethod;

	private final WebTarget target;

	private final Collection<Action> actions = new TreeSet<>();

	private final int parametersCount;

	RestMethod( Class cls, Method method, WebTarget target )
	{
		this.cls = cls;
		this.method = method;
		this.returnType = getReturnType( cls, method );
		this.httpMethod = getHttpMethod( method );
		this.target = addPathFromAnnotation( method, target );

		if( this.httpMethod == null ) {
			if( this.target == target ) {
				throw new UnsupportedOperationException( "Not a resource method." );
			}
			else if( !this.returnType.isInterface() ) {
				throw new UnsupportedOperationException( "Return type not an interface" );
			}
		}

		final Class<?>[] types = method.getParameterTypes();
		final int entIndex = -1;

		this.parametersCount = types.length;

		for( int k = 0, z = types.length; k < z; k++ ) {
			this.actions.add( new SetValueAction( k ) );

			final Annotation[] pas = method.getParameterAnnotations()[k];

			for( final Annotation a : pas ) {
				final Action action = findAction( a, k );

				if( action != null ) {
					this.actions.add( action );
				}
			}

			if( pas.length == 0 ) {
				final Type entityType = GenericTypeReflector.getExactParameterTypes( method, cls )[entIndex];

				this.actions.add( new EntityAction( entityType, k ) );
			}
		}

		final Produces produces = getAnnotation( Produces.class );

		if( produces != null ) {
			this.actions.add( new ProducesAction( produces ) );
		}

		final Consumes consumes = getAnnotation( Consumes.class );
		if( consumes != null ) {
			this.actions.add( new ConsumesAction( consumes ) );
		}
	}

	Object call( Object[] arguments, MultivaluedMap<String, Object> headers, Collection<Cookie> cookies, Form form )
	{
		if( this.httpMethod == null ) {
			final RestClientIH ih = new RestClientIH( this.returnType, this.target, headers, cookies, form );

			return RestClientIH.newProxy( this.returnType, ih );
		}

		final RestContext cx = new RestContext( this.target, headers, cookies, form );

		this.actions.forEach( a -> {
			a.prepare( cx, arguments );
		} );

		final Invocation.Builder b = cx.target.request( cx.accepts.toArray( new String[0] ) ).headers( cx.headers );

		cx.cookies.forEach( c -> b.cookie( c ) );

		if( cx.entity != null ) {
			return b.method( this.httpMethod, Entity.entity( cx.entity, cx.contentType ), this.returnType );
		}
		else {
			return b.method( this.httpMethod, this.returnType );
		}
	}

	<T extends Annotation> T getAnnotation( Class cls, Class<T> annCls )
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

	private Action findAction( Annotation a, int ix )
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

		return null;
	}

	private <T extends Annotation> T getAnnotation( Class<T> annCls )
	{
		final T annot = this.method.getAnnotation( annCls );

		if( annot != null ) {
			return annot;
		}

		return getAnnotation( this.cls, annCls );
	}

	private Object getEntity( Object[] arguments )
	{
		return this.entityIndex < 0 ? null : arguments[this.entityIndex];
	}
}

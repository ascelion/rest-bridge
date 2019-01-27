
package ascelion.utils.etc;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ascelion.rest.bridge.client.RBUtils;

import static ascelion.utils.etc.Secured.runPrivileged;
import static ascelion.utils.etc.Secured.runPrivilegedWithException;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

import lombok.Getter;

public final class PropDescriptor<T>
{

	interface SetOp
	{

		void apply( Object o, Object v ) throws Exception;
	}

	interface GetOp
	{

		Object apply( Object o ) throws Exception;
	}

	private final SetOp sop;
	private final GetOp gop;
	@Getter
	private final Class<T> type;
	private final Class<?> declType;
	@Getter
	private final String name;
	private final Map<Class<? extends Annotation>, Set<Annotation>> annotations = new IdentityHashMap<>();

	PropDescriptor( Field field )
	{
		this.type = (Class<T>) field.getType();
		this.declType = field.getDeclaringClass();
		this.sop = isFinal( field.getModifiers() ) ? null : ( o, v ) -> field.set( o, v );
		this.gop = ( o ) -> field.get( o );
		this.name = field.getName();

		runPrivileged( () -> field.setAccessible( true ) );

		addAnnotations( field.getAnnotations() );
	}

	PropDescriptor( PropertyDescriptor pd )
	{
		final Optional<Method> wmet = ofNullable( pd.getWriteMethod() );
		final Optional<Method> rmet = ofNullable( pd.getReadMethod() );

		this.type = (Class<T>) pd.getPropertyType();

		final Class<?> wcls = wmet.map( Method::getDeclaringClass ).orElse( null );
		final Class<?> rcls = rmet.map( Method::getDeclaringClass ).orElse( null );

		if( wcls != null && rcls != null ) {
			if( wcls.isAssignableFrom( rcls ) ) {
				this.declType = rcls;
			}
			else {
				this.declType = wcls;
			}
		}
		else if( wcls != null ) {
			this.declType = wcls;
		}
		else {
			this.declType = rcls;
		}

		this.sop = wmet.map( m -> (SetOp) ( o, v ) -> m.invoke( o, v ) ).orElse( null );
		this.gop = rmet.map( m -> (GetOp) ( o ) -> m.invoke( o ) ).orElse( null );

		final StringBuilder b;

		if( wmet.isPresent() ) {
			b = new StringBuilder( wmet.get().getName().substring( 3 ) );
		}
		else {
			b = new StringBuilder( rmet.get().getName().replaceAll( "^(get|is)", "" ) );
		}

		b.setCharAt( 0, Character.toLowerCase( b.charAt( 0 ) ) );

		this.name = b.toString();

		wmet.map( Method::getAnnotations )
			.ifPresent( this::addAnnotations );
		rmet.map( Method::getAnnotations )
			.ifPresent( this::addAnnotations );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		if( isReadable() ) {
			sb.append( "R" );
		}
		if( isWritable() ) {
			sb.append( "W" );
		}

		return format( "%s(%s): %s", this.name, sb, this.type.getName() );
	}

	public Class<?> getDeclaringClass()
	{
		return this.declType;
	}

	public boolean isWritable()
	{
		return this.sop != null;
	}

	public boolean isReadable()
	{
		return this.gop != null;
	}

	public boolean isAnnotationPresent( Class<? extends Annotation> type )
	{
		return this.annotations.containsKey( type );
	}

	public Annotation[] getAnnotations()
	{
		return this.annotations.values().stream()
			.flatMap( Set::stream )
			.distinct()
			.toArray( Annotation[]::new );
	}

	public <A extends Annotation> A[] getAnnotations( Class<A> type )
	{
		return this.annotations.getOrDefault( type, emptySet() )
			.toArray( (A[]) Array.newInstance( type, 0 ) );
	}

	public <A extends Annotation> A getAnnotation( Class<A> type )
	{
		return (A) this.annotations.getOrDefault( type, emptySet() ).stream()
			.findFirst()
			.orElse( null );
	}

	public void set( Object o, Object v )
	{
		if( this.sop == null ) {
			throw new UnsupportedOperationException( format( "Property %s.%s is not writable", this.type.getName(), this.name ) );
		}

		try {
			runPrivilegedWithException( () -> this.sop.apply( o, v ) );
		}
		catch( final PrivilegedActionException e ) {
			throw RBUtils.wrapException( e.getCause(), format( "Cannot write property %s.%s", this.type.getName(), this.name ) );
		}
	}

	public Object get( Object o )
	{
		if( this.gop == null ) {
			throw new UnsupportedOperationException( format( "Property %s.%s is not readable", this.type.getName(), this.name ) );
		}

		try {
			return runPrivilegedWithException( () -> this.gop.apply( o ) );
		}
		catch( final PrivilegedActionException e ) {
			throw RBUtils.wrapException( e.getCause(), format( "Cannot read property %s.%s", this.type.getName(), this.name ) );
		}
	}

	void addAnnotations( Annotation[] v )
	{
		stream( v ).forEach( this::addAnnotation );
	}

	private void addAnnotation( Annotation a )
	{
		this.annotations
			.computeIfAbsent( a.annotationType(), t -> new HashSet<>() )
			.add( a );
	}
}


package ascelion.utils.etc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.google.common.collect.MapMaker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

public final class SimpleTypeBuilder
{

	static private final String[] CREATE_METHODS = { "valueOf", "parse", "create", "from", "fromValue" };
	static private final Map<Class<?>, Function<String, ?>> BUILDERS = new IdentityHashMap<>();

	static {
		BUILDERS.put( boolean.class, Boolean::valueOf );
		BUILDERS.put( Boolean.class, Boolean::valueOf );
		BUILDERS.put( char.class, s -> s.charAt( 0 ) );
		BUILDERS.put( Character.class, s -> s.charAt( 0 ) );
		BUILDERS.put( int.class, Integer::valueOf );
		BUILDERS.put( Integer.class, Integer::valueOf );
		BUILDERS.put( long.class, Long::valueOf );
		BUILDERS.put( Long.class, Long::valueOf );
		BUILDERS.put( float.class, Float::valueOf );
		BUILDERS.put( Float.class, Float::valueOf );
		BUILDERS.put( double.class, Double::valueOf );
		BUILDERS.put( Double.class, Double::valueOf );
		BUILDERS.put( BigInteger.class, BigInteger::new );
		BUILDERS.put( BigDecimal.class, BigDecimal::new );
	}

	@RequiredArgsConstructor
	static private class CtBuilder<T> implements Function<String, T>
	{

		private final Constructor<T> ct;

		@Override
		@SneakyThrows
		public T apply( String t )
		{
			return this.ct.newInstance( t );
		}
	}

	@RequiredArgsConstructor
	static private class MtBuilder<T> implements Function<String, T>
	{

		private final Method mt;

		@Override
		@SneakyThrows
		public T apply( String t )
		{
			return (T) this.mt.invoke( null, t );
		}
	}

	private final Map<Class<?>, Function<String, ?>> builders = new MapMaker().weakKeys().makeMap();

	public SimpleTypeBuilder()
	{
		this.builders.putAll( BUILDERS );
	}

	public void addBuilder( Class<?> type, Function<String, ?> bld )
	{
		this.builders.put( type, bld );
	}

	public <T> Function<String, ?> getBuilder( Class<T> type )
	{
		return this.builders.computeIfAbsent( type, this::newBuilder );
	}

	public <T> T createFromPlainText( Class<T> type, String value )
	{
		value = trimToNull( value );

		if( value == null ) {
			return null;
		}

		final Function<String, T> bld = (Function<String, T>) getBuilder( type );

		if( bld == null ) {
			throw new RuntimeException( format( "Cannot construct %s from string", type ) );
		}

		return bld.apply( value );
	}

	private <T> Function<String, T> newBuilder( Class<T> type )
	{
		try {
			return new CtBuilder<>( type.getConstructor( String.class ) );
		}
		catch( final NoSuchMethodException e ) {
			;
		}

		for( final String cm : CREATE_METHODS ) {
			try {
				return new MtBuilder<>( type.getMethod( cm, String.class ) );
			}
			catch( final NoSuchMethodException e ) {
				;
			}
		}

		return null;
	}
}

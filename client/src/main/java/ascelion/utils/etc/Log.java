
package ascelion.utils.etc;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor( access = AccessLevel.PRIVATE )
public final class Log
{

	static public final Log get()
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		return get( ste.getClassName() );
	}

	static public Log get( String cat )
	{
		return new Log( Logger.getLogger( cat ) );
	}

	private final Logger sys;

	public void trace( Supplier<String> sup )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.FINEST, ste.getClassName(), ste.getMethodName(), sup );
	}

	public void trace( String fmt, Object... args )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.FINEST, ste.getClassName(), ste.getMethodName(), () -> format( fmt, args ) );
	}

	public void debug( Supplier<String> sup )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.FINE, ste.getClassName(), ste.getMethodName(), sup );
	}

	public void debug( String fmt, Object... args )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.FINE, ste.getClassName(), ste.getMethodName(), () -> format( fmt, args ) );
	}

	public void info( Supplier<String> sup )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.INFO, ste.getClassName(), ste.getMethodName(), sup );
	}

	public void info( String fmt, Object... args )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.INFO, ste.getClassName(), ste.getMethodName(), () -> format( fmt, args ) );
	}

	public void warn( Supplier<String> sup )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.WARNING, ste.getClassName(), ste.getMethodName(), sup );
	}

	public void warn( String fmt, Object... args )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.WARNING, ste.getClassName(), ste.getMethodName(), () -> format( fmt, args ) );
	}

	public void warn( Throwable t, Supplier<String> sup )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.WARNING, ste.getClassName(), ste.getMethodName(), t, sup );
	}

	public void warn( Throwable t, String fmt, Object... args )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.WARNING, ste.getClassName(), ste.getMethodName(), t, () -> format( fmt, args ) );
	}

	public void error( Supplier<String> sup )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.SEVERE, ste.getClassName(), ste.getMethodName(), sup );
	}

	public void error( String fmt, Object... args )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.SEVERE, ste.getClassName(), ste.getMethodName(), () -> format( fmt, args ) );
	}

	public void error( Throwable t, Supplier<String> sup )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.SEVERE, ste.getClassName(), ste.getMethodName(), t, sup );
	}

	public void error( Throwable t, String fmt, Object... args )
	{
		final StackTraceElement ste = new Throwable().getStackTrace()[1];

		this.sys.logp( Level.SEVERE, ste.getClassName(), ste.getMethodName(), t, () -> format( fmt, args ) );
	}

	public String getName()
	{
		return this.sys.getName();
	}

}

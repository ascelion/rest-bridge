
package ascelion.rest.micro.tests.shared;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class SLF4JHandler extends Handler
{

	static private final String UNKNOWN_LOGGER_NAME = "unknown.jul.logger";
	static private final String JUL_FQCN = "java.util.logging.Logger";

	static private final int JUL_TRACE_LEVEL = Level.FINEST.intValue();
	static private final int JUL_DEBUG_LEVEL = Level.FINE.intValue();
	static private final int JUL_INFO_LEVEL = Level.INFO.intValue();
	static private final int JUL_WARN_LEVEL = Level.WARNING.intValue();
	static private final int JUL_OFF_LEVEL = Level.OFF.intValue();

	public static void install()
	{
		final java.util.logging.Logger root = LogManager.getLogManager().getLogger( "" );

		Stream.of( root.getHandlers() ).forEach( root::removeHandler );

		root.setLevel( Level.ALL );
		root.addHandler( new SLF4JHandler() );
	}

	@Override
	public void publish( LogRecord record )
	{
		if( record == null ) {
			return;
		}

		final int julLevel = record.getLevel().intValue();

		if( julLevel == JUL_OFF_LEVEL ) {
			return;
		}

		final Logger slf4j = getLogger( record );
		final String l10n = getL10N( record );

		if( slf4j instanceof LocationAwareLogger ) {
			( (LocationAwareLogger) slf4j ).log( null, JUL_FQCN, toLAL( julLevel ), l10n, null, record.getThrown() );
		}
		else {
			if( julLevel <= JUL_TRACE_LEVEL ) {
				slf4j.trace( l10n, record.getThrown() );
			}
			else if( julLevel <= JUL_DEBUG_LEVEL ) {
				slf4j.debug( l10n, record.getThrown() );
			}
			else if( julLevel <= JUL_INFO_LEVEL ) {
				slf4j.info( l10n, record.getThrown() );
			}
			else if( julLevel <= JUL_WARN_LEVEL ) {
				slf4j.warn( l10n, record.getThrown() );
			}
			else {
				slf4j.error( l10n, record.getThrown() );
			}
		}
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void close() throws SecurityException
	{
	}

	private String getL10N( LogRecord record )
	{
		String msg = record.getMessage();

		if( msg == null ) {
			return null;
		}

		final ResourceBundle bundle = record.getResourceBundle();

		if( bundle != null ) {
			try {
				msg = bundle.getString( msg );
			}
			catch( final MissingResourceException e ) {
			}
		}

		final Object[] params = record.getParameters();

		if( params != null && params.length > 0 ) {
			try {
				msg = MessageFormat.format( msg, params );
			}
			catch( final IllegalArgumentException e ) {
				// no formatting
				return msg;
			}
		}

		return msg;
	}

	private int toLAL( int julLevel )
	{
		if( julLevel <= JUL_TRACE_LEVEL ) {
			return LocationAwareLogger.TRACE_INT;
		}
		else if( julLevel <= JUL_DEBUG_LEVEL ) {
			return LocationAwareLogger.DEBUG_INT;
		}
		else if( julLevel <= JUL_INFO_LEVEL ) {
			return LocationAwareLogger.INFO_INT;
		}
		else if( julLevel <= JUL_WARN_LEVEL ) {
			return LocationAwareLogger.WARN_INT;
		}
		else {
			return LocationAwareLogger.ERROR_INT;
		}
	}

	private Logger getLogger( LogRecord rec )
	{
		String name = rec.getLoggerName();

		if( name == null ) {
			name = UNKNOWN_LOGGER_NAME;
		}

		return LoggerFactory.getLogger( name );
	}

}

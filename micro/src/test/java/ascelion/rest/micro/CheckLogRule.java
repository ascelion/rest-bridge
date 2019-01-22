
package ascelion.rest.micro;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

public class CheckLogRule implements TestRule
{

	private final Logger logger = (Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME );
	private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

	@Override
	public Statement apply( Statement base, Description description )
	{
		return new Statement()
		{

			@Override
			public void evaluate() throws Throwable
			{
				setUp();
				base.evaluate();
			}
		};
	}

	public List<ILoggingEvent> getEvents()
	{
		return unmodifiableList( this.appender.list );
	}

	public List<ILoggingEvent> getEvents( Level level )
	{
		return getEvents()
			.stream()
			.filter( e -> e.getLevel().isGreaterOrEqual( level ) )
			.collect( toList() );
	}

	public List<ILoggingEvent> getEvents( String name, Level level )
	{
		return getEvents()
			.stream()
			.filter( e -> e.getLevel().isGreaterOrEqual( level ) )
			.filter( e -> e.getLoggerName().equals( name ) )
			.collect( toList() );
	}

	private void setUp()
	{
		this.logger.detachAndStopAllAppenders();
		this.logger.addAppender( this.appender );
		this.logger.setLevel( Level.ALL );

		this.appender.clearAllFilters();
		this.appender.list.clear();
		this.appender.start();
	}
}

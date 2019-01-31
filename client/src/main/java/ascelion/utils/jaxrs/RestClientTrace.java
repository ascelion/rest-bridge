
package ascelion.utils.jaxrs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestRequestContext;

import static ascelion.rest.bridge.client.RestClientProperties.DEFAULT_CONTENT_TYPE;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RestClientTrace implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor
{

	@RequiredArgsConstructor
	@EqualsAndHashCode
	static class Header implements Comparable<Header>
	{

		final String name;
		final String value;

		@Override
		public int compareTo( Header that )
		{
			if( that == null ) {
				return 1;
			}

			final int dif1 = String.CASE_INSENSITIVE_ORDER.compare( this.name, that.name );

			if( dif1 != 0 ) {
				return dif1;
			}

			return String.CASE_INSENSITIVE_ORDER.compare( this.value, that.value );
		}

		@Override
		public String toString()
		{
			return format( "%s: %s", this.name, this.value );
		}
	}

	static private final String REQ_ST_PROP = "ascelion.rest.bridge.trace.request.stream";
	static private final String REQ_OK_PROP = "ascelion.rest.bridge.trace.request.ok";

	@RequiredArgsConstructor
	static class OLogStream extends OutputStream
	{

		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		final OutputStream out;
		final MediaType mt;
		final Formatter fmt;

		@Override
		public void write( int b ) throws IOException
		{
			this.out.write( b );
			this.buf.write( b );
		}

		@Override
		public void write( byte[] b, int off, int len ) throws IOException
		{
			this.out.write( b, off, len );
			this.buf.write( b, off, len );
		}

		@Override
		public void flush() throws IOException
		{
			this.out.flush();
		}

		@Override
		public void close() throws IOException
		{
			try( OutputStream o = this.out ) {
				this.out.flush();
			}
		}
	}

	static private final String INI_PREFIX = "*";
	static private final String REQ_PREFIX = ">";
	static private final String RSP_PREFIX = "<";

	static private final Logger L = Logger.getLogger( "ascelion.rest.bridge.TRAFFIC" );
	static private final AtomicLong ID = new AtomicLong();

	private final Logger log;
	private final Level lev;

	public RestClientTrace()
	{
		this( L, Level.FINEST );
	}

	@Override
	public void filter( ClientRequestContext reqx ) throws IOException
	{
		if( !this.log.isLoggable( this.lev ) ) {
			return;
		}

		ID.incrementAndGet();
		final Formatter fmt = new Formatter();

		printLine( fmt, INI_PREFIX, "==================================" );
		printLine( fmt, INI_PREFIX, "%s", RestRequestContext.getJavaMethod() );
		printLine( fmt, REQ_PREFIX, "%s %s", reqx.getMethod(), reqx.getUri() );
		printHeaders( fmt, REQ_PREFIX, reqx.getStringHeaders() );

		if( reqx.hasEntity() ) {
			final MediaType mt = mediaType( reqx.getHeaderString( CONTENT_TYPE ), reqx.getConfiguration() );

			if( RBUtils.isTextContent( mt ) ) {
				final OutputStream st = new OLogStream( reqx.getEntityStream(), mt, fmt );

				reqx.setEntityStream( st );
				reqx.setProperty( REQ_ST_PROP, st );
			}
		}
		else {
			doLog( fmt );
		}

		reqx.setProperty( REQ_OK_PROP, true );
	}

	@Override
	public void aroundWriteTo( WriterInterceptorContext wcx ) throws IOException, WebApplicationException
	{
		try {
			wcx.proceed();
		}
		finally {
			final OLogStream out = (OLogStream) wcx.getProperty( REQ_ST_PROP );

			if( out != null ) {
				printBody( out.fmt, REQ_PREFIX, out.buf.toByteArray(), out.mt );

				doLog( out.fmt );
			}
		}
	}

	@Override
	public void filter( ClientRequestContext reqx, ClientResponseContext rspx ) throws IOException
	{
		if( !this.log.isLoggable( this.lev ) ) {
			return;
		}

		final Formatter fmt = new Formatter();
		final Boolean reqok = (Boolean) reqx.getProperty( REQ_OK_PROP );

		if( reqok == null || !reqok ) {
			printLine( fmt, INI_PREFIX, "??????????????????????????????????" );
			printLine( fmt, INI_PREFIX, "%s", RestRequestContext.getJavaMethod() );
			printLine( fmt, REQ_PREFIX, "%s %s", reqx.getMethod(), reqx.getUri() );
			printHeaders( fmt, REQ_PREFIX, reqx.getStringHeaders() );
		}

		printLine( fmt, INI_PREFIX, "----------------------------------" );
		printLine( fmt, RSP_PREFIX, "%03d %s", rspx.getStatus(), rspx.getStatusInfo().getReasonPhrase() );
		printHeaders( fmt, RSP_PREFIX, rspx.getHeaders() );

		if( rspx.getEntityStream() != null ) {
			final MediaType mt = mediaType( rspx.getHeaderString( CONTENT_TYPE ), reqx.getConfiguration() );

			if( RBUtils.isTextContent( mt ) ) {
				try {
					final byte[] body = toByteArray( rspx.getEntityStream() );

					rspx.setEntityStream( new ByteArrayInputStream( body ) );

					printBody( fmt, RSP_PREFIX, body, mt );
				}
				catch( final Exception e ) {
					printLine( fmt, RSP_PREFIX, "!!! cannot read body: %s", e.getMessage() );
				}
			}
		}

		doLog( fmt );
	}

	private void doLog( Formatter fmt )
	{
		this.log.log( this.lev, "\n" + fmt );
	}

	private void printLine( Formatter fmt, String prefix, String format, Object... objects )
	{
		fmt.format( "%d %s ", ID.get(), prefix )
			.format( format, objects )
			.format( "\n" );
	}

	private void printHeaders( Formatter fmt, String prefix, MultivaluedMap<String, String> multivaluedMap )
	{
		multivaluedMap.entrySet().stream()
			.flatMap( e -> e.getValue().stream().map( v -> new Header( e.getKey(), v ) ) )
			.sorted()
			.forEach( h -> printLine( fmt, prefix, "%s", h ) );
		;
	}

	private void printBody( Formatter fmt, String prefix, byte[] body, MediaType mt )
	{
		try {
			readLines( new ByteArrayInputStream( body ), RBUtils.charset( mt ) )
				.forEach( line -> printLine( fmt, prefix, "%s", line ) );
		}
		catch( final IOException e ) {
			printLine( fmt, prefix, "cannot print body: %s", e );
		}
	}

	private MediaType mediaType( String ct, Configuration cf )
	{
		if( isBlank( ct ) ) {
			ct = ofNullable( cf )
				.map( c -> c.getProperty( DEFAULT_CONTENT_TYPE ) )
				.map( Object::toString )
				.orElse( MediaType.TEXT_PLAIN );
		}

		try {
			return MediaType.valueOf( ct );
		}
		catch( final IllegalArgumentException e ) {
			return null;
		}
	}
}

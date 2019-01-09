
package ascelion.rest.bridge.etc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import ascelion.rest.bridge.client.Util;

import static ascelion.rest.bridge.client.RestClientProperties.DEFAULT_CONTENT_TYPE;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.RequiredArgsConstructor;

@Provider
@Priority( Integer.MAX_VALUE )
@RequiredArgsConstructor
public class RestClientTrace implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor
{

	static private final String REQ_ST_PROP = "ascelion.rest.micro.tests.shared.trace.request.stream";
	static private final String REQ_MT_PROP = "ascelion.rest.micro.tests.shared.trace.request.mediaType";

	@RequiredArgsConstructor
	static class OLogStream extends OutputStream
	{

		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		final OutputStream out;
		final Formatter fmt;

		@Override
		public void write( byte[] b, int off, int len ) throws IOException
		{
			this.out.write( b, off, len );
			this.buf.write( b, off, len );
		}

		@Override
		public void write( int b ) throws IOException
		{
			this.out.write( b );
			this.buf.write( b );
		}
	}

	static private final String INI_PREFIX = "*";
	static private final String REQ_PREFIX = ">";
	static private final String RSP_PREFIX = "<";

	static private final Logger L = Logger.getLogger( "ascelion.bridge.tests.CLIENT" );
	static private final AtomicLong ID = new AtomicLong();

	static private boolean isTextContent( MediaType mt )
	{
		return ( mt != null ) &&
			( mt.getType().equals( "text" )
				|| mt.equals( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
				|| mt.getSubtype().contains( "xml" )
				|| mt.getSubtype().contains( "json" ) );
	}

	private final Logger log;
	private final Level lev;

	public RestClientTrace()
	{
		this( L, Level.INFO );
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
		printLine( fmt, REQ_PREFIX, "%s %s", reqx.getMethod(), reqx.getUri() );
		printHeaders( fmt, REQ_PREFIX, reqx.getStringHeaders() );

		if( reqx.hasEntity() ) {
			final MediaType mt = mediaType( reqx.getHeaderString( CONTENT_TYPE ), reqx.getConfiguration() );

			if( RestClientTrace.isTextContent( mt ) ) {
				final OutputStream st = new OLogStream( reqx.getEntityStream(), fmt );

				reqx.setEntityStream( st );
				reqx.setProperty( REQ_ST_PROP, st );
				reqx.setProperty( REQ_MT_PROP, mt );
			}
		}
		else {
			doLog( fmt );
		}
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
				printBody( out.fmt, REQ_PREFIX, out.buf.toByteArray(), (MediaType) wcx.getProperty( REQ_MT_PROP ) );

				doLog( out.fmt );
			}
		}
	}

	@Override
	public void filter( ClientRequestContext reqx, ClientResponseContext rspx ) throws IOException
	{
		final Formatter fmt = new Formatter();

		printLine( fmt, INI_PREFIX, "----------------------------------" );
		printLine( fmt, RSP_PREFIX, "%03d %s", rspx.getStatus(), rspx.getStatusInfo().getReasonPhrase() );
		printHeaders( fmt, RSP_PREFIX, rspx.getHeaders() );

		if( rspx.hasEntity() ) {
			final MediaType mt = mediaType( rspx.getHeaderString( CONTENT_TYPE ), reqx.getConfiguration() );

			if( RestClientTrace.isTextContent( mt ) ) {
				final byte[] body = toByteArray( rspx.getEntityStream() );

				printBody( fmt, RSP_PREFIX, body, mt );

				rspx.setEntityStream( new ByteArrayInputStream( body ) );
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
			.sorted( ( e1, e2 ) -> CASE_INSENSITIVE_ORDER.compare( e1.getKey(), e2.getKey() ) )
			.forEach( e -> printLine( fmt, prefix, "%s: %s", e.getKey(), e.getValue().stream().collect( joining( ", " ) ) ) );
		;
	}

	private void printBody( Formatter fmt, String prefix, byte[] body, MediaType mt )
	{
		try {
			readLines( new ByteArrayInputStream( body ), Util.charset( mt ) )
				.forEach( line -> printLine( fmt, prefix, "%s", line ) );
		}
		catch( final IOException e ) {
			printLine( fmt, prefix, "cannot print body: %s", e );
		}
	}

	private MediaType mediaType( String ct, Configuration cf )
	{
		if( isBlank( ct ) ) {
			ct = ofNullable( cf.getProperty( DEFAULT_CONTENT_TYPE ) )
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

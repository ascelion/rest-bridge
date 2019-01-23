
package ascelion.rest.micro;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ascelion.rest.bridge.client.RestRequestContext;

import static java.util.stream.Collectors.toMap;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class HttpHeadersImpl implements HttpHeaders
{

	private final RestRequestContext rc;

	@Override
	public List<String> getRequestHeader( String name )
	{
		return this.rc.getHeaders().get( name );
	}

	@Override
	public String getHeaderString( String name )
	{
		return this.rc.getHeaders().getFirst( name );
	}

	@Override
	public MultivaluedMap<String, String> getRequestHeaders()
	{
		return new ROMultivaluedMap<>( this.rc.getHeaders() );
	}

	@Override
	public List<MediaType> getAcceptableMediaTypes()
	{
		// TODO
		throw new UnsupportedOperationException( "TODO" );
	}

	@Override
	public List<Locale> getAcceptableLanguages()
	{
		// TODO
		throw new UnsupportedOperationException( "TODO" );
	}

	@Override
	public MediaType getMediaType()
	{
		// TODO
		throw new UnsupportedOperationException( "TODO" );
	}

	@Override
	public Locale getLanguage()
	{
		// TODO
		throw new UnsupportedOperationException( "TODO" );
	}

	@Override
	public Map<String, Cookie> getCookies()
	{
		return this.rc.getCookies().stream()
			.collect( toMap( c -> c.getName(), Function.identity() ) );
	}

	@Override
	public Date getDate()
	{
		// TODO
		throw new UnsupportedOperationException( "TODO" );
	}

	@Override
	public int getLength()
	{
		// TODO
		throw new UnsupportedOperationException( "TODO" );
	}
}

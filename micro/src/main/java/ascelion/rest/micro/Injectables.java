
package ascelion.rest.micro;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
final class Injectables
{

	static private class DefHttpHeaders implements HttpHeaders
	{

		@Override
		public List<String> getRequestHeader( String name )
		{
			return emptyList();
		}

		@Override
		public String getHeaderString( String name )
		{
			return null;
		}

		@Override
		public MultivaluedMap<String, String> getRequestHeaders()
		{
			return ROMultivaluedMap.empty();
		}

		@Override
		public List<MediaType> getAcceptableMediaTypes()
		{
			return singletonList( MediaType.WILDCARD_TYPE );
		}

		@Override
		public List<Locale> getAcceptableLanguages()
		{
			return singletonList( new Locale( "*" ) );
		}

		@Override
		public MediaType getMediaType()
		{
			return null;
		}

		@Override
		public Locale getLanguage()
		{
			return null;
		}

		@Override
		public Map<String, Cookie> getCookies()
		{
			return emptyMap();
		}

		@Override
		public Date getDate()
		{
			return null;
		}

		@Override
		public int getLength()
		{
			return -1;
		}
	}

	static Map<Class<?>, Supplier<?>> SUPPLIERS;

	static {
		final Map<Class<?>, Supplier<?>> map = new HashMap<>();

		map.put( HttpHeaders.class, DefHttpHeaders::new );

		SUPPLIERS = unmodifiableMap( map );
	}

	static <X> X getDefault( Class<X> type )
	{
		final Supplier<X> sup = (Supplier<X>) SUPPLIERS.get( type );

		if( sup == null ) {
			throw new IllegalStateException( "No supplier for type " + type.getName() );
		}

		return sup.get();
	}
}

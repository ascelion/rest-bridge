
package ascelion.rest.micro;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

class DefaultExceptionMapper implements ResponseExceptionMapper<Throwable>
{

	static final String CONFIG_KEY_DISABLE_MAPPER = "microprofile.rest.client.disable.default.mapper";

	@Override
	public Throwable toThrowable( Response response )
	{
		return new WebApplicationException( response );
	}

}

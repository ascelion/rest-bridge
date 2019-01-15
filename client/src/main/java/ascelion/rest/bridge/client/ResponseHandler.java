
package ascelion.rest.bridge.client;

import javax.ws.rs.core.Response;

public interface ResponseHandler
{

	ResponseHandler NONE = rsp -> {
	};

	void handleResponse( Response rsp ) throws Exception;
}

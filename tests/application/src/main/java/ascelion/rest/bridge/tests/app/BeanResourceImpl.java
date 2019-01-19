
package ascelion.rest.bridge.tests.app;

import ascelion.rest.bridge.tests.api.BeanParamData;
import ascelion.rest.bridge.tests.api.BeanResource;

public class BeanResourceImpl
implements BeanResource
{

	@Override
	public BeanParamData get( BeanParamData request )
	{
		return request;
	}

}

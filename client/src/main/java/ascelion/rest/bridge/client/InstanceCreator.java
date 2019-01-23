
package ascelion.rest.bridge.client;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;

public class InstanceCreator implements Extension
{

	void afterDeploymentValidation( @Observes AfterDeploymentValidation event )
	{
	}
}


package ascelion.rest.bridge.tests.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class Factories
{

	@Produces
	private final ExecutorService exec = Executors.newFixedThreadPool( 2 );
}

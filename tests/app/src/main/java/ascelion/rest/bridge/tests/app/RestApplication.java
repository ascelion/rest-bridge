
package ascelion.rest.bridge.tests.app;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import ascelion.rest.bridge.tests.api.API;

import lombok.Getter;

@ApplicationPath( API.BASE )
@ApplicationScoped
@Getter
public class RestApplication
extends Application
{
}

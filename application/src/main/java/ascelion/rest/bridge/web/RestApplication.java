
package ascelion.rest.bridge.web;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath( RestApplication.BASE )
public class RestApplication
extends Application
{

	static public final String BASE = "rest";
}

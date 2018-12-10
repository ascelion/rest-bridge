
package ascelion.rest.bridge

import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator

class Glassfish extends Server {

	Glassfish( String name, ServersContainer sc, Instantiator instantiator ) {
		super( name, sc, instantiator )

		adminPort = 4848
	}

	File getExecutable() {
		final StringBuilder path = new StringBuilder()
				.append( getHome() )
				.append( File.separator )
				.append( "bin" )
				.append( File.separator )
				.append( "asadmin" )

		if( OperatingSystem.current().isWindows() ) {
			path.append( ".bat" )
		}

		return new File( path.toString() )
	}
}

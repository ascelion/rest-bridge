
package ascelion.rest.bridge

import org.gradle.api.Task
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecSpec

class Glassfish extends Server {

	String domain = 'domain1'

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

	@Override
	boolean isServerUp() {
		OutputStream bo = new ByteArrayOutputStream()

		this.project.exec { x ->
			x.standardOutput = bo

			cliLocal(x, "list-domains" )
		}

		return bo as String ==~ /(?ms).*^${this.domain} running$.*/
	}

	@Override
	protected void configureStart( Task task ) {
		super.configureStart( task )

		task.doLast {
			this.project.exec { x ->
				println "Starting domain ${this.domain}"

				cliLocal( x, "start-domain", this.domain )
			}
		}
	}

	@Override
	protected void configureStop( Task task ) {
		super.configureStop( task )

		task.doLast {
			this.project.exec { x ->
				println "Stopping domain ${this.domain}"

				cliLocal( x, "stop-domain", this.domain )
			}
		}
	}

	private void cli( ExecSpec spec, Object... arguments ) {
		spec.executable = this.executable

		if( host != null ) {
			spec.args( "--host=${this.host}" )

			withAuth( spec )
		}

		spec.args( "--port=${this.adminPort}" )

		spec.args( arguments )
	}

	private void cliLocal( ExecSpec spec, Object... arguments ) {
		spec.executable = this.executable

		spec.args( arguments )
	}
}

package ascelion.rest.bridge

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class SpawnAS extends DefaultTask {
	@Input
	private String serverType

	@TaskAction
	void run() {
		println "HIHI: ${serverType}"
	}

	public void serverType( String serverType ) {
		this.serverType = serverType
	}

	public void setServerType( String serverType ) {
		this.serverType = serverType
	}
}

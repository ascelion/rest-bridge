pipeline {
	agent any
	options {
		disableConcurrentBuilds()
		timeout time: 15, unit: 'MINUTES'
		buildDiscarder logRotator( daysToKeepStr: "14" )
	}
	tools {
		jdk 'J8'
	}
	environment {
		JENKINS_NODE_COOKIE = 'dontkillme'
	}

	stages {
		stage('Build') {
			steps {
				sh "chmod +x gradlew"
				sh "./gradlew cV clean build -xcheck"
			}
		}
		stage('Checks') {
			steps {
				sh "./gradlew check --continue"
				junit allowEmptyResults: true, testResults: "**/TEST-*.xml"
				step([$class: 'Publisher', reportFilenamePattern: '**/testng-results.xml'])
			}
		}
		stage('Publish') {
			steps {
				sh "./gradlew publishLocal publish -xcheck "
			}
		}
		stage('Archive') {
			steps {
				archiveArtifacts fingerprint: true,
				artifacts: '**/build/publications/*/pom-*.xml, **/build/libs/*.jar'
			}
		}
	}
}

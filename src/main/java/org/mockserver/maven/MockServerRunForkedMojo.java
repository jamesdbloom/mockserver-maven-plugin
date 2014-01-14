package org.mockserver.maven;

import com.google.common.annotations.VisibleForTesting;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jamesdbloom
 */
@Mojo(name = "runForked", requiresProject = false, threadSafe = false)
public class MockServerRunForkedMojo extends MockServerAbstractMojo {

    /**
     * Get a list of artifacts used by this plugin
     */
    @Parameter(defaultValue = "${plugin.artifacts}", required = true, readonly = true)
    protected List<Artifact> pluginArtifacts;
    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component
    protected RepositorySystem repositorySystem;
    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component
    protected ArtifactResolver artifactResolver;

    public static String fileSeparators(String path) {
        StringBuilder ret = new StringBuilder();
        for (char c : path.toCharArray()) {
            if ((c == '/') || (c == '\\')) {
                ret.append(File.separatorChar);
            } else {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping plugin execution");
        } else {
            getLog().info("Starting MockServer on port " + serverPort);
            List<String> arguments = new ArrayList<>(Arrays.asList(getJavaBin(),
                    "-Dmockserver.logLevel=" + logLevel,
                    "-Dmockserver.serverStopPort=" + serverStopPort,
//                    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006",
                    "-jar", jarWithDependencies()));
            if (serverPort != -1) {
                arguments.add("-serverPort");
                arguments.add("" + serverPort);
            }
            if (serverSecurePort != -1) {
                arguments.add("-serverSecurePort");
                arguments.add("" + serverSecurePort);
            }
            if (proxyPort != -1) {
                arguments.add("-proxyPort");
                arguments.add("" + proxyPort);
            }
            if (proxySecurePort != -1) {
                arguments.add("-proxySecurePort");
                arguments.add("" + proxySecurePort);
            }
            ProcessBuilder processBuilder = newProcessBuilder(arguments);
            if (pipeLogToConsole) {
                processBuilder.inheritIO();
            }
            try {
                processBuilder.start();
            } catch (IOException e) {
                getLog().error("Exception while starting MockServer", e);
            }
        }

    }

    @VisibleForTesting
    ProcessBuilder newProcessBuilder(List<String> arguments) {
        return new ProcessBuilder(arguments);
    }

    @VisibleForTesting
    String getJavaBin() {
        String javaBinary = "java";

        File javaHomeDirectory = new File(System.getProperty("java.home"));
        for (String javaExecutable : new String[]{"java", "java.exe"}) {
            File javaExeLocation = new File(javaHomeDirectory, fileSeparators("bin/" + javaExecutable));
            if (javaExeLocation.exists() && javaExeLocation.isFile()) {
                javaBinary = javaExeLocation.getAbsolutePath();
                break;
            }
        }

        return javaBinary;
    }

    @VisibleForTesting
    String jarWithDependencies() {
        Artifact jarWithDependencies = repositorySystem.createArtifactWithClassifier("org.mock-server", "mockserver-jetty", "2.2-SNAPSHOT", "jar", "jar-with-dependencies");
        artifactResolver.resolve(new ArtifactResolutionRequest().setArtifact(jarWithDependencies));
        getLog().debug("Running MockServer using " + jarWithDependencies.getFile().getAbsolutePath());
        return jarWithDependencies.getFile().getAbsolutePath();
    }
}

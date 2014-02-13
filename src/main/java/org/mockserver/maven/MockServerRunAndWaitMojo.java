package org.mockserver.maven;

import com.google.common.annotations.VisibleForTesting;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.mockserver.logging.Logging;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Run the MockServer and wait for a specified timeout (or indefinitely)
 *
 * @author jamesdbloom
 */
@Mojo(name = "run", requiresProject = false, threadSafe = false)
public class MockServerRunAndWaitMojo extends MockServerAbstractMojo {

    public void execute() throws MojoExecutionException {
        Logging.overrideLogLevel(logLevel);
        if (skip) {
            getLog().info("Skipping plugin execution");
        } else {
            if (getLog().isInfoEnabled()) {
                getLog().info("Starting MockServer on"
                        + (serverPort != -1 ? " serverPort " + serverPort : "")
                        + (serverSecurePort != -1 ? " serverSecurePort " + serverSecurePort : "")
                        + (proxyPort != -1 ? " proxyPort " + proxyPort : "")
                        + (proxySecurePort != -1 ? " proxySecurePort " + proxySecurePort : "")
                );
            }
            try {
                if (timeout > 0) {
                    getEmbeddedJettyHolder().start(serverPort, serverSecurePort, proxyPort, proxySecurePort);
                    try {
                        newSettableFuture().get(timeout, TimeUnit.SECONDS);
                    } catch (TimeoutException te) {
                        // do nothing this is an expected exception when the timeout expires
                    }
                } else {
                    getEmbeddedJettyHolder().start(serverPort, serverSecurePort, proxyPort, proxySecurePort);
                    newSettableFuture().get();
                }
            } catch (Exception e) {
                getLog().error("Exception while running MockServer", e);
            }
        }

    }

    @VisibleForTesting
    SettableFuture<Object> newSettableFuture() {
        return SettableFuture.create();
    }
}

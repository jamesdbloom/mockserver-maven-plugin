package org.mockserver.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockserver.mockserver.MockServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author jamesdbloom
 */
public class MockServerRunAndWaitMojoTest {

    @Mock
    private SettableFuture<Object> objectSettableFuture;
    @Mock
    private InstanceHolder mockEmbeddedJettyHolder;
    @Mock
    private MockServer mockServerRunner;
    @InjectMocks
    private MockServerRunAndWaitMojo mockServerRunAndWaitMojo = new MockServerRunAndWaitMojo();

    @Before
    public void setupMocks() {
        initMocks(this);

        MockServerAbstractMojo.embeddedJettyHolder = mockEmbeddedJettyHolder;
    }

    @Test
    public void shouldRunMockServerAndWaitIndefinitely() throws MojoExecutionException, ExecutionException, InterruptedException {
        // given
        mockServerRunAndWaitMojo.serverPort = "1,2";
        mockServerRunAndWaitMojo.proxyPort = 3;
        mockServerRunAndWaitMojo.timeout = 0;
        mockServerRunAndWaitMojo.initializationClass = "org.mockserver.maven.ExampleInitializationClass";

        // when
        mockServerRunAndWaitMojo.execute();

        // then
        verify(mockEmbeddedJettyHolder).start(eq(new Integer[]{1,2}), eq(3), any(ExampleInitializationClass.class));
        verify(objectSettableFuture).get();
    }

    @Test
    public void shouldRunMockServerAndWaitIndefinitelyAndHandleInterruptedException() throws MojoExecutionException, ExecutionException, InterruptedException {
        // given
        mockServerRunAndWaitMojo.serverPort = "1";
        mockServerRunAndWaitMojo.proxyPort = 3;
        mockServerRunAndWaitMojo.timeout = 0;
        doThrow(new InterruptedException("TEST EXCEPTION")).when(objectSettableFuture).get();

        // when
        mockServerRunAndWaitMojo.execute();
    }

    @Test
    public void shouldRunMockServerAndWaitForFixedPeriod() throws MojoExecutionException, ExecutionException, InterruptedException, TimeoutException {
        // given
        mockServerRunAndWaitMojo.serverPort = "1,2";
        mockServerRunAndWaitMojo.proxyPort = 3;
        mockServerRunAndWaitMojo.timeout = 2;
        mockServerRunAndWaitMojo.initializationClass = "org.mockserver.maven.ExampleInitializationClass";

        // when
        mockServerRunAndWaitMojo.execute();

        // then
        verify(mockEmbeddedJettyHolder).start(eq(new Integer[]{1,2}), eq(3), any(ExampleInitializationClass.class));
        verify(objectSettableFuture).get(2, TimeUnit.SECONDS);
    }

    @Test
    public void shouldRunMockServerAndWaitForFixedPeriodAndHandleInterruptedException() throws MojoExecutionException, ExecutionException, InterruptedException, TimeoutException {
        // given
        mockServerRunAndWaitMojo.serverPort = "1";
        mockServerRunAndWaitMojo.proxyPort = 3;
        mockServerRunAndWaitMojo.timeout = 2;
        when(objectSettableFuture.get(2, TimeUnit.SECONDS)).thenThrow(new InterruptedException("TEST EXCEPTION"));

        // when
        mockServerRunAndWaitMojo.execute();
    }

    @Test
    public void shouldSkipStoppingMockServer() throws MojoExecutionException {
        // given
        mockServerRunAndWaitMojo.skip = true;

        // when
        mockServerRunAndWaitMojo.execute();

        // then
        verifyNoMoreInteractions(mockEmbeddedJettyHolder);
    }
}

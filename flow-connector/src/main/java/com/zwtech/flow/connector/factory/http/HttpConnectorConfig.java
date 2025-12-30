package com.zwtech.flow.connector.factory.http;

import java.time.Duration;

/**
 * @author renc
 */
public final class HttpConnectorConfig {

    private Duration connectionTimeout = Duration.ofSeconds(5);

    private Duration responseTimeout = Duration.ofSeconds(10);

    /**
     * Specifies whether compression (gzip, Brotli, and zstd) is enabled.
     *
     * <p>Note: Brotli and zstd compressions require additional dependencies.
     * <p>Note: For zstd compression, {@literal Accept-Encoding: zstd} header needs to be added explicitly.
     */
    private boolean compressionEnabled;

    /**
     * Option to disable {@code retry once} support for the outgoing requests that fail with
     * {@link reactor.netty.channel.AbortedException#isConnectionReset(Throwable)}.
     * <p>By default, this is set to false in which case {@code retry once} is enabled.
     */
    private boolean retryDisabled;

    /**
     * Whether to disable certificate validation for the http client.
     */
    private boolean certVerifyDisabled;

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public boolean isRetryDisabled() {
        return retryDisabled;
    }

    public void setRetryDisabled(boolean retryDisabled) {
        this.retryDisabled = retryDisabled;
    }

    public boolean isCertVerifyDisabled() {
        return certVerifyDisabled;
    }

    public HttpConnectorConfig setCertVerifyDisabled(boolean certVerifyDisabled) {
        this.certVerifyDisabled = certVerifyDisabled;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        HttpConnectorConfig that = (HttpConnectorConfig) o;
        return compressionEnabled == that.compressionEnabled && retryDisabled == that.retryDisabled && certVerifyDisabled == that.certVerifyDisabled && connectionTimeout.equals(that.connectionTimeout) && responseTimeout.equals(that.responseTimeout);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + connectionTimeout.hashCode();
        result = 31 * result + responseTimeout.hashCode();
        result = 31 * result + Boolean.hashCode(compressionEnabled);
        result = 31 * result + Boolean.hashCode(retryDisabled);
        result = 31 * result + Boolean.hashCode(certVerifyDisabled);
        return result;
    }
}

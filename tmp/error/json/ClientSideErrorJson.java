package edu.mayo.ingenium.module.error.json;

import com.google.common.base.Objects;

/**
 * Input fields for Request Body when saving client-side errors.
 */
public final class ClientSideErrorJson {
    private String url;
    private String stacktrace;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ClientSideErrorJson that = (ClientSideErrorJson) o;
        return Objects.equal(getUrl(), that.getUrl()) &&
               Objects.equal(getStacktrace(), that.getStacktrace());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUrl(), getStacktrace());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(final String stacktrace) {
        this.stacktrace = stacktrace;
    }
}

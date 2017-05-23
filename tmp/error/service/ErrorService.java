package edu.mayo.ingenium.module.error.service;

import static com.github.choonchernlim.betterPreconditions.preconditions.PreconditionFactory.expect;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import edu.mayo.ingenium.module.error.json.ClientSideErrorJson;
import edu.mayo.ingenium.module.principal.service.PrincipalService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * This class handles exception thrown on server side and client side.
 */
@Service
public class ErrorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorService.class);

    private final PrincipalService principalService;

    @Autowired
    public ErrorService(final PrincipalService principalService) {
        this.principalService = principalService;
    }

    /**
     * Captures server side related exception info and display them in log.
     *
     * @param request   HTTP Servlet request
     * @param exception Exception thrown
     * @return Error map
     */
    public Map<String, String> handleServerSideException(final HttpServletRequest request, final Exception exception) {
        expect(request, "request").not().toBeNull().check();
        expect(exception, "exception").not().toBeNull().check();

        final ImmutableMap.Builder<String, String> builder = getErrorMapBuilder(request);
        builder.put(ErrorConstant.REQUEST_URL, request.getRequestURL().toString());
        builder.put(ErrorConstant.REQUEST_PARAMETERS, prettyPrintMap(getRequestParameterMap(request), 1));
        builder.put(ErrorConstant.REQUEST_BODY, getRequestBody(request));
        builder.put(ErrorConstant.REQUEST_HEADERS, prettyPrintMap(getRequestHeaderMap(request), 1));
        builder.put(ErrorConstant.JAVA_STACKTRACE, ExceptionUtils.getStackTrace(exception));

        return handleException(builder.build());
    }

    /**
     * Captures client side related exception info and display them in log.
     *
     * @param request             HTTP Servlet request
     * @param clientSideErrorJson Client side error JSON
     * @return Error map
     */
    public Map<String, String> handleClientSideException(final HttpServletRequest request,
                                                         final ClientSideErrorJson clientSideErrorJson) {
        expect(request, "request").not().toBeNull().check();
        expect(clientSideErrorJson, "clientSideErrorJson").not().toBeNull().check();

        final ImmutableMap.Builder<String, String> builder = getErrorMapBuilder(request);
        builder.put(ErrorConstant.REQUEST_URL, clientSideErrorJson.getUrl());
        builder.put(ErrorConstant.REQUEST_HEADERS, prettyPrintMap(getRequestHeaderMap(request), 1));
        builder.put(ErrorConstant.JAVASCRIPT_STACKTRACE, clientSideErrorJson.getStacktrace());

        return handleException(builder.build());
    }

    /**
     * A common place to handle both server side and client side exceptions.
     *
     * @param errorMap Error map
     * @return Same error map for method chain-ability
     */
    private Map<String, String> handleException(final ImmutableMap<String, String> errorMap) {
        // TODO LIMC Per Corey, just log it for now... may need to revisit before production release
        LOGGER.error("\n=====================================" +
                     "\nAn unexpected error has occurred..." +
                     "\n=====================================" +
                     "\n" + prettyPrintMap(errorMap, 0));

        return errorMap;
    }

    /**
     * Returns error map builder containing info that are global to both client side and server side errors.
     *
     * @param request HTTP Servlet Request
     * @return Error map builer
     */
    private ImmutableMap.Builder<String, String> getErrorMapBuilder(final HttpServletRequest request) {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put(ErrorConstant.DATETIME, DateTime.now().toString());
        builder.put(ErrorConstant.USER_LAN_ID, principalService.getPrincipal().getLanId());
        builder.put(ErrorConstant.USER_HOST_NAME, request.getRemoteHost());
        return builder;
    }

    /**
     * Returns request body as string.
     *
     * @param request HTTP Servlet Request
     * @return Request body as string
     */
    private String getRequestBody(final HttpServletRequest request) {
        String requestBody;

        try {
            requestBody = IOUtils.toString(request.getReader());
        }
        catch (Exception e) {
            requestBody = null;
        }

        return StringUtils.defaultIfBlank(requestBody, "-");
    }

    /**
     * Returns request headers as string.
     *
     * @param request HTTP Servlet Request
     * @return Request headers as string
     */
    private ImmutableMap<String, String> getRequestHeaderMap(final HttpServletRequest request) {
        final Enumeration<String> names = request.getHeaderNames();
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        while (names.hasMoreElements()) {
            final String key = names.nextElement();
            final String value = StringUtils.defaultIfBlank(request.getHeader(key), "-");
            builder.put(key, value);
        }

        return builder.build();
    }

    /**
     * Returns request parameters as string.
     *
     * @param request HTTP Servlet Request
     * @return Request parameters as string
     */
    private ImmutableMap<String, String> getRequestParameterMap(final HttpServletRequest request) {
        final Enumeration<String> names = request.getParameterNames();
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        while (names.hasMoreElements()) {
            final String key = names.nextElement();
            final String value = StringUtils.defaultIfBlank(request.getParameter(key), "-");
            builder.put(key, value);
        }

        return builder.build();
    }

    /**
     * Returns formatted map as string.
     *
     * @param map               Map
     * @param totalIndentations Total indentations
     * @return Formatted map as string
     */
    private String prettyPrintMap(final ImmutableMap<String, String> map, final Integer totalIndentations) {
        final String value = Joiner
                .on("\n" + getIndentations(totalIndentations))
                .withKeyValueSeparator("\n" + getIndentations(totalIndentations + 1))
                .join(map);

        return StringUtils.defaultIfBlank(value, "-");
    }

    /**
     * Returns indentations.
     *
     * @param totalIndentations Total indentations needed
     * @return Indentations
     */
    private String getIndentations(final Integer totalIndentations) {
        return Strings.repeat("\t", totalIndentations);
    }
}

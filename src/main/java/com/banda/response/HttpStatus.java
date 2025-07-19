package com.banda.response;


/**
 * HTTP response status codes with a short meaning.
 * Based on RFC 9110 (HTTP Semantics) and related RFCs (6585, 7238, 8297, 8470).
 */
public enum HttpStatus {

    // --- 1xx Informational ---
    CONTINUE(100, "Continue", "Client may proceed (body follows)"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols", "Server agrees to upgrade"),
    PROCESSING(102, "Processing", "WebDAV: Still handling"),
    EARLY_HINTS(103, "Early Hints", "Preload resources"),

    // --- 2xx Success ---
    OK(200, "OK", "Request succeeded"),
    CREATED(201, "Created", "Resource created"),
    ACCEPTED(202, "Accepted", "Will process later"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative", "Modified proxy result"),
    NO_CONTENT(204, "No Content", "Success, no body"),
    RESET_CONTENT(205, "Reset Content", "Reset document/view"),
    PARTIAL_CONTENT(206, "Partial Content", "Partial range sent"),
    MULTI_STATUS(207, "Multi-Status", "WebDAV: Multiple results"),
    ALREADY_REPORTED(208, "Already Reported", "WebDAV: DAV binding repeated"),
    IM_USED(226, "IM Used", "Delta applied"),

    // --- 3xx Redirection ---
    MULTIPLE_CHOICES(300, "Multiple Choices", "Options for resource"),
    MOVED_PERMANENTLY(301, "Moved Permanently", "Use new URI forever"),
    FOUND(302, "Found", "Temporary redirect"),
    SEE_OTHER(303, "See Other", "Get with GET at other URI"),
    NOT_MODIFIED(304, "Not Modified", "Use cached version"),
    USE_PROXY(305, "Use Proxy", "Deprecated proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect", "Repeat with same method"),
    PERMANENT_REDIRECT(308, "Permanent Redirect", "Repeat forever (same method)"),

    // --- 4xx Client Errors ---
    BAD_REQUEST(400, "Bad Request", "Malformed request"),
    UNAUTHORIZED(401, "Unauthorized", "Auth required"),
    PAYMENT_REQUIRED(402, "Payment Required", "Reserved / future"),
    FORBIDDEN(403, "Forbidden", "Refused despite auth"),
    NOT_FOUND(404, "Not Found", "No matching resource"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", "Method blocked"),
    NOT_ACCEPTABLE(406, "Not Acceptable", "No acceptable representation"),
    PROXY_AUTH_REQUIRED(407, "Proxy Auth Required", "Authenticate to proxy"),
    REQUEST_TIMEOUT(408, "Request Timeout", "Client too slow"),
    CONFLICT(409, "Conflict", "State conflict"),
    GONE(410, "Gone", "Resource removed"),
    LENGTH_REQUIRED(411, "Length Required", "Need Content-Length"),
    PRECONDITION_FAILED(412, "Precondition Failed", "ETag/If-* failed"),
    CONTENT_TOO_LARGE(413, "Content Too Large", "Body too big"), // RFC 9110 (was Payload Too Large)
    URI_TOO_LONG(414, "URI Too Long", "URI length exceeded"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type", "Format unsupported"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable", "Invalid range"),
    EXPECTATION_FAILED(417, "Expectation Failed", "Expect header failed"),
    IM_A_TEAPOT(418, "I'm a teapot", "RFC joke / Easter egg"),
    MISDIRECTED_REQUEST(421, "Misdirected Request", "Wrong origin"),
    UNPROCESSABLE_CONTENT(422, "Unprocessable Content", "Semantics invalid"),
    LOCKED(423, "Locked", "WebDAV: Resource locked"),
    FAILED_DEPENDENCY(424, "Failed Dependency", "WebDAV: Dependency failed"),
    TOO_EARLY(425, "Too Early", "Risk of replay"),
    UPGRADE_REQUIRED(426, "Upgrade Required", "Use newer protocol"),
    PRECONDITION_REQUIRED(428, "Precondition Required", "Need conditional request"),
    TOO_MANY_REQUESTS(429, "Too Many Requests", "Rate limited"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Req Header Fields Too Large", "Headers too large"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons", "Blocked/legal"),

    // --- 5xx Server Errors ---
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", "Server fault"),
    NOT_IMPLEMENTED(501, "Not Implemented", "Not supported"),
    BAD_GATEWAY(502, "Bad Gateway", "Upstream error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable", "Temporarily overloaded"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout", "Upstream timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported", "Version refused"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates", "Negotiation loop"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage", "WebDAV: No space"),
    LOOP_DETECTED(508, "Loop Detected", "WebDAV: Infinite loop"),
    NOT_EXTENDED(510, "Not Extended", "Further extensions needed"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Auth Required", "Authenticate to network");

    private final int code;
    private final String reason;
    private final String meaning;

    HttpStatus(int code, String reason, String meaning) {
        this.code = code;
        this.reason = reason;
        this.meaning = meaning;
    }

    public int code() { return code; }
    public String reason() { return reason; }
    public String meaning() { return meaning; }

    // Class (1xx, 2xx etc.)
    public int series() { return code / 100; }

    public boolean isInformational() { return series() == 1; }
    public boolean isSuccess()       { return series() == 2; }
    public boolean isRedirection()   { return series() == 3; }
    public boolean isClientError()   { return series() == 4; }
    public boolean isServerError()   { return series() == 5; }
    public boolean isError()         { return code >= 400; }

    // Reverse lookup
    public static HttpStatus from(int code) {
        for (HttpStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }

    @Override
    public String toString() {
        return code + " " + reason;
    }
}

package spark;

import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

/**
 * FileName: SparkFilter
 * Author:   MAIBENBEN
 * Date:     2020/8/4 10:00
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class BasicAuth implements Filter {
    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(BasicAuth.class);

    private static final String CHARSET = "UTF-8";

    private String username = "";

    private String password = "";

    private String realm = "Protected";

    @Override
    public void init(FilterConfig filterConfig) {
        username = filterConfig.getInitParameter("username");
        password = filterConfig.getInitParameter("password");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {

            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {

                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {

                    try {
                        String credentials = new String(Base64.decodeBase64(st.nextToken()), CHARSET);
                        LOG.debug("Credentials: " + credentials);

                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _username = credentials.substring(0, p).trim();
                            String _password = credentials.substring(p + 1).trim();


                            if (!username.equals(_username) || !password.equals(_password)) {
                                unauthorized(response, "Bad credentials");
                            }

                            filterChain.doFilter(servletRequest, servletResponse);
                        } else {
                            unauthorized(response, "Invalid authentication token");
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }
                }
            }
        } else {
            unauthorized(response);
        }
    }

    @Override
    public void destroy() {
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

    public static void main(String[] args) {

    }
}

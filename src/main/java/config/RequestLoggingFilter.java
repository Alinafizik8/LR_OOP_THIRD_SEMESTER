package config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (httpRequest.getRequestURI().contains("/from-math")) {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);

            System.out.println("=== INCOMING REQUEST TO /from-math ===");
            System.out.println("Content-Type: " + httpRequest.getContentType());

            chain.doFilter(wrappedRequest, response);

            byte[] content = wrappedRequest.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                System.out.println("Request Body: " + body);
            }
            System.out.println("=== END REQUEST ===");
        } else {
            chain.doFilter(request, response);
        }
    }
}

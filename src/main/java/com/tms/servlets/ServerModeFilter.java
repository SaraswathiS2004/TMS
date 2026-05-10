package com.tms.servlets;

import com.tms.sheet.ServerMode;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

public class ServerModeFilter implements Filter {

    // These endpoints must work even in READ_ONLY mode
    private static final Set<String> EXEMPT_SUFFIXES = Set.of(
        "/api/admin/set-mode",
        "/api/admin/restore-from-sheet"
    );

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (ServerMode.getCurrent() == ServerMode.Mode.READ_ONLY
                && !"GET".equalsIgnoreCase(request.getMethod())) {
            String uri = request.getRequestURI();
            boolean exempt = EXEMPT_SUFFIXES.stream().anyMatch(uri::endsWith);
            if (!exempt) {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                    "{\"status\":\"FAIL\",\"message\":\"Server is in READ_ONLY mode. No write operations allowed.\"}"
                );
                return;
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}

package com.example.application.ai.mcp;

import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;

/**
 * The MCP add-on demo: Claude Desktop on the trade-show laptop drives the app's UI through draiv's tools,
 * over {@code http://localhost:8888/mcp}. Only with the {@code mcp} profile (see
 * {@code application-mcp.properties}); without it the MCP server stays off.
 * <p>
 * Whoever reaches the endpoint drives the most recently opened browser tab, so it answers only requests
 * from this machine: the phone in the booth's hotspot reaches the app, but not the MCP endpoint.
 */
@Configuration
@Profile("mcp")
public class McpDemoConfig {

    private static final Logger log = LoggerFactory.getLogger(McpDemoConfig.class);

    /**
     * draiv's UI tools ({@code inspect}, {@code apply}, {@code apply_many}, {@code list_capabilities},
     * {@code navigate}) for the MCP server, which collects {@link ToolCallbackProvider} beans. Not the
     * script tools: a macro could click what an {@code AgentScope} hides.
     */
    @Bean
    ToolCallbackProvider mcpTools(CapabilityMcpTools capabilityTools) {
        return MethodToolCallbackProvider.builder().toolObjects(capabilityTools).build();
    }

    @Bean
    FilterRegistrationBean<LocalhostOnlyFilter> mcpLocalhostOnly() {
        var registration = new FilterRegistrationBean<>(new LocalhostOnlyFilter());
        // /sse too, in case the protocol property is lost and the SSE transport comes back
        registration.addUrlPatterns("/mcp", "/mcp/*", "/sse", "/sse/*");
        registration.setName("mcpLocalhostOnly");
        return registration;
    }

    /** Refuses every MCP request that does not come from this machine. */
    static class LocalhostOnlyFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            if (isLoopback(request.getRemoteAddr())) {
                chain.doFilter(request, response);
            } else {
                log.warn("Refused MCP request from {}", request.getRemoteAddr());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "MCP is only available on this machine");
            }
        }

        static boolean isLoopback(String address) {
            if (address == null) {
                return false;
            }
            try {
                return InetAddress.ofLiteral(address).isLoopbackAddress();
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}

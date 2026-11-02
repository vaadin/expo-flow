package com.example.application.ai.mcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpDemoConfigTest {

    @Test
    void onlyThisMachineIsLoopback() {
        assertTrue(McpDemoConfig.LocalhostOnlyFilter.isLoopback("127.0.0.1"));
        assertTrue(McpDemoConfig.LocalhostOnlyFilter.isLoopback("0:0:0:0:0:0:0:1"));
        assertFalse(McpDemoConfig.LocalhostOnlyFilter.isLoopback("192.168.1.23")); // the phone in the hotspot
        assertFalse(McpDemoConfig.LocalhostOnlyFilter.isLoopback("localhost")); // never resolve names
        assertFalse(McpDemoConfig.LocalhostOnlyFilter.isLoopback(null));
    }
}

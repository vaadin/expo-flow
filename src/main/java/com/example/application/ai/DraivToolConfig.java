package com.example.application.ai;

import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.presenter.PresenterToolCatalog;
import com.vaadin.draiv.ai.script.DraivScriptTools;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.session.DrivenUiResolver;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The draiv wiring the app supplies: the Spring AI tools the AI may call. Everything else — the UI
 * registry, provider selection and the {@code TextPresenterService} chat loop — is auto-configured by
 * draiv-ai once a bean named {@code chatToolCatalog} exists. Both the W-JAX talk search and the
 * AI Driven UI chat use this one catalog.
 */
@Configuration
public class DraivToolConfig {

    /**
     * draiv's generic UI tools: {@code inspect} (read the component tree on screen), {@code apply} /
     * {@code apply_many} (set a field, click a button), {@code list_capabilities} and {@code navigate}.
     * Plus the script tools ({@code list_scripts}, {@code get_script}, {@code save_script},
     * {@code run_script}) for the macro tab.
     * <p>
     * The script tools are built here rather than declared as a bean: the MCP server picks up tool
     * beans, and these should only reach the in-app chat.
     */
    @Bean(name = "chatToolCatalog")
    PresenterToolCatalog chatToolCatalog(CapabilityMcpTools capabilityTools, ScriptStore scriptStore,
                                         DrivenUiResolver uiResolver) {
        return new PresenterToolCatalog(MethodToolCallbackProvider.builder()
                .toolObjects(capabilityTools, new DraivScriptTools(scriptStore, uiResolver))
                .build());
    }

    /** draiv ships no script persistence; the macros live in memory until a restart. */
    @Bean
    ScriptStore scriptStore() {
        return new InMemoryScriptStore();
    }
}

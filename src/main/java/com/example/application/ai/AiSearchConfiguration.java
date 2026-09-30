package com.example.application.ai;

import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.presenter.PresenterToolCatalog;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The one piece of draiv wiring the app supplies: the Spring AI tools the AI may call. Everything
 * else — the UI registry, provider selection and the {@code TextPresenterService} chat loop — is
 * auto-configured by draiv-ai once a bean named {@code chatToolCatalog} exists.
 */
@Configuration
public class AiSearchConfiguration {

    /**
     * draiv's generic UI tools: {@code inspect} (read the component tree on screen), {@code apply} /
     * {@code apply_many} (set a field, click a button), {@code list_capabilities} and {@code navigate}.
     */
    @Bean(name = "chatToolCatalog")
    PresenterToolCatalog chatToolCatalog(CapabilityMcpTools capabilityTools) {
        return new PresenterToolCatalog(MethodToolCallbackProvider.builder()
                .toolObjects(capabilityTools)
                .build());
    }
}

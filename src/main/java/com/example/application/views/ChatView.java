package com.example.application.views;

import org.springframework.ai.chat.model.ChatModel;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListVariant;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Chat")
@Menu(title = "Chat", icon = LineAwesomeIconUrl.COMMENTS_SOLID, order = 4)
@Route(value = "chat")
public class ChatView extends UploadDropZone {

    public ChatView(ChatModel chatModel) {
        setSizeFull();

        var layout = new VerticalLayout();
        layout.setSizeFull();
        setContent(layout);

        var messageList = new MessageList();
        messageList.setSizeFull();
        messageList.setMarkdown(true);
        var messageInput = new MessageInput();

        // Upload for attachments
        var uploadManager = new UploadManager(this);
        uploadManager.setMaxFiles(5);
        uploadManager.setMaxFileSize(5 * 1024 * 1024); // 5 MB
        uploadManager.setAcceptedMimeTypes("image/*", "application/pdf",
                "text/plain");
        setUploadManager(uploadManager);

        var uploadButton = new UploadButton(uploadManager);
        uploadButton.setIcon(VaadinIcon.UPLOAD.create());

        var inputLayout = new HorizontalLayout(uploadButton, messageInput);
        inputLayout.setWidthFull();
        inputLayout.setFlexGrow(1, messageInput);
        inputLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        inputLayout.setSpacing(false);

        var uploadFileList = new UploadFileList(uploadManager);
        uploadFileList.getElement().getStyle().setWidth("100%");
        uploadFileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        var bottomLayout = new VerticalLayout(uploadFileList, inputLayout);
        bottomLayout.setPadding(false);

        layout.add(messageList, bottomLayout);
        layout.setFlexGrow(1, messageList);
        layout.setFlexShrink(0, bottomLayout);

        // Create LLM provider
        var provider = new SpringAILLMProvider(chatModel);

        AIOrchestrator.builder(provider,
                        "You are an unhelpful and sarcastic assistant.")
                .withMessageList(messageList)
                .withInput(messageInput)
                .withFileReceiver(uploadManager)
                .build();
    }
}

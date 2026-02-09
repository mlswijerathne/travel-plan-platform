package com.travelplan.aiagent.service;

import com.google.adk.events.Event;
import com.travelplan.aiagent.dto.ChatStreamEvent;
import com.travelplan.aiagent.dto.QuickReplyChip;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class StreamingService {

    private static final Pattern CHIP_PATTERN = Pattern.compile("\\[chip:\\s*(.+?)\\]");

    public Flux<ServerSentEvent<ChatStreamEvent>> convertToSSE(Flowable<Event> events, String sessionId) {
        return Flux.from(events)
                .map(event -> processEvent(event, sessionId))
                .filter(java.util.Objects::nonNull)
                .concatWith(Flux.just(ServerSentEvent.<ChatStreamEvent>builder()
                        .event("done")
                        .data(ChatStreamEvent.done(sessionId))
                        .build()));
    }

    private ServerSentEvent<ChatStreamEvent> processEvent(Event event, String sessionId) {
        try {
            // Check for agent transfer via EventActions
            if (event.actions() != null && event.actions().transferToAgent().isPresent()) {
                String targetAgent = event.actions().transferToAgent().get();
                log.debug("Agent transfer to: {}", targetAgent);
                return ServerSentEvent.<ChatStreamEvent>builder()
                        .event("agent_transfer")
                        .data(ChatStreamEvent.agentTransfer(targetAgent))
                        .build();
            }

            // Check for tool calls in function call events
            if (event.functionCalls() != null && !event.functionCalls().isEmpty()) {
                String toolName = event.functionCalls().get(0).name().orElse("unknown");
                String agentName = event.author();
                log.debug("Tool call: {} by agent: {}", toolName, agentName);
                return ServerSentEvent.<ChatStreamEvent>builder()
                        .event("tool_call")
                        .data(ChatStreamEvent.toolCall(toolName, agentName))
                        .build();
            }

            // Check for content (text response)
            String content = event.stringifyContent();
            if (content != null && !content.isBlank()) {
                if (event.finalResponse()) {
                    // Parse quick reply chips from content
                    List<QuickReplyChip> chips = extractQuickReplyChips(content);
                    String cleanContent = removeChipMarkup(content);

                    log.debug("Final response from agent: {}, chips: {}", event.author(), chips.size());
                    return ServerSentEvent.<ChatStreamEvent>builder()
                            .event("final_response")
                            .data(ChatStreamEvent.finalResponse(cleanContent, chips, null, sessionId))
                            .build();
                } else {
                    return ServerSentEvent.<ChatStreamEvent>builder()
                            .event("text_delta")
                            .data(ChatStreamEvent.textDelta(content))
                            .build();
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage());
            return ServerSentEvent.<ChatStreamEvent>builder()
                    .event("error")
                    .data(ChatStreamEvent.error("Error processing response"))
                    .build();
        }
    }

    public List<QuickReplyChip> extractQuickReplyChips(String content) {
        List<QuickReplyChip> chips = new ArrayList<>();
        Matcher matcher = CHIP_PATTERN.matcher(content);

        while (matcher.find()) {
            String label = matcher.group(1).trim();
            chips.add(QuickReplyChip.builder()
                    .label(label)
                    .value(label)
                    .build());
        }

        return chips;
    }

    public String removeChipMarkup(String content) {
        return CHIP_PATTERN.matcher(content).replaceAll("").trim();
    }
}

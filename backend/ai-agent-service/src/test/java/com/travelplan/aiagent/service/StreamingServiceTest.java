package com.travelplan.aiagent.service;

import com.travelplan.aiagent.dto.QuickReplyChip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreamingServiceTest {

    private StreamingService streamingService;

    @BeforeEach
    void setUp() {
        streamingService = new StreamingService();
    }

    @Test
    void extractQuickReplyChips_findsChips() {
        String content = "Here are some suggestions:\n[chip: Show me hotels]\n[chip: Find a tour guide]";

        List<QuickReplyChip> chips = streamingService.extractQuickReplyChips(content);

        assertEquals(2, chips.size());
        assertEquals("Show me hotels", chips.get(0).getLabel());
        assertEquals("Show me hotels", chips.get(0).getValue());
        assertEquals("Find a tour guide", chips.get(1).getLabel());
    }

    @Test
    void extractQuickReplyChips_noChips_returnsEmpty() {
        String content = "Just a regular response without chips.";

        List<QuickReplyChip> chips = streamingService.extractQuickReplyChips(content);

        assertTrue(chips.isEmpty());
    }

    @Test
    void removeChipMarkup_removesAllChips() {
        String content = "Suggestions:\n[chip: Option A]\n[chip: Option B]\nEnd.";

        String clean = streamingService.removeChipMarkup(content);

        assertFalse(clean.contains("[chip:"));
        assertTrue(clean.contains("Suggestions:"));
        assertTrue(clean.contains("End."));
    }

    @Test
    void removeChipMarkup_noChips_returnsOriginal() {
        String content = "No chips here.";

        String clean = streamingService.removeChipMarkup(content);

        assertEquals("No chips here.", clean);
    }

    @Test
    void extractQuickReplyChips_handlesWhitespaceVariations() {
        String content = "[chip:NoSpace] [chip: LeadingSpace] [chip:  ExtraSpaces  ]";

        List<QuickReplyChip> chips = streamingService.extractQuickReplyChips(content);

        assertEquals(3, chips.size());
        assertEquals("NoSpace", chips.get(0).getLabel());
        assertEquals("LeadingSpace", chips.get(1).getLabel());
        assertEquals("ExtraSpaces", chips.get(2).getLabel());
    }
}

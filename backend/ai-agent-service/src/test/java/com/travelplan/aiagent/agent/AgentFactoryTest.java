package com.travelplan.aiagent.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.models.BaseLlm;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentFactoryTest {

    private AgentFactory agentFactory;
    private BaseLlm model;

    @BeforeEach
    void setUp() {
        agentFactory = new AgentFactory();
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey("test-key")
                .modelName("gpt-4o-mini")
                .build();
        model = new LangChain4j(chatModel);
    }

    @Test
    void hotelSearchAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.hotelSearchAgent(model);
        assertEquals("HotelSearchAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void tourGuideSearchAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.tourGuideSearchAgent(model);
        assertEquals("TourGuideSearchAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void vehicleSearchAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.vehicleSearchAgent(model);
        assertEquals("VehicleSearchAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void itineraryGeneratorAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.itineraryGeneratorAgent(model);
        assertEquals("ItineraryGeneratorAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void budgetAnalyzerAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.budgetAnalyzerAgent(model);
        assertEquals("BudgetAnalyzerAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void tripPlannerAgent_isCreatedWithSubAgents() {
        LlmAgent hotelAgent = agentFactory.hotelSearchAgent(model);
        LlmAgent tourGuideAgent = agentFactory.tourGuideSearchAgent(model);
        LlmAgent vehicleAgent = agentFactory.vehicleSearchAgent(model);
        LlmAgent itineraryAgent = agentFactory.itineraryGeneratorAgent(model);
        LlmAgent budgetAgent = agentFactory.budgetAnalyzerAgent(model);
        LlmAgent eventProductAgent = agentFactory.eventProductSearchAgent(model);

        BaseAgent rootAgent = agentFactory.tripPlannerAgent(
                model, hotelAgent, tourGuideAgent, vehicleAgent, itineraryAgent, budgetAgent, eventProductAgent);

        assertNotNull(rootAgent);
        assertEquals("TripPlannerAgent", rootAgent.name());
    }

    @Test
    void allAgents_areCreatedSuccessfully() {
        LlmAgent hotel = agentFactory.hotelSearchAgent(model);
        LlmAgent tourGuide = agentFactory.tourGuideSearchAgent(model);
        LlmAgent vehicle = agentFactory.vehicleSearchAgent(model);
        LlmAgent itinerary = agentFactory.itineraryGeneratorAgent(model);
        LlmAgent budget = agentFactory.budgetAnalyzerAgent(model);

        assertNotNull(hotel);
        assertNotNull(tourGuide);
        assertNotNull(vehicle);
        assertNotNull(itinerary);
        assertNotNull(budget);
    }
}

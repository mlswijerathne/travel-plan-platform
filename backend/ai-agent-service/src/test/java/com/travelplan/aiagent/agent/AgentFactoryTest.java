package com.travelplan.aiagent.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentFactoryTest {

    private AgentFactory agentFactory;
    private static final String MODEL = "gemini-2.0-flash";

    @BeforeEach
    void setUp() {
        agentFactory = new AgentFactory();
    }

    @Test
    void hotelSearchAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.hotelSearchAgent(MODEL);
        assertEquals("HotelSearchAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void tourGuideSearchAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.tourGuideSearchAgent(MODEL);
        assertEquals("TourGuideSearchAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void vehicleSearchAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.vehicleSearchAgent(MODEL);
        assertEquals("VehicleSearchAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void itineraryGeneratorAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.itineraryGeneratorAgent(MODEL);
        assertEquals("ItineraryGeneratorAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void budgetAnalyzerAgent_isCreatedWithCorrectName() {
        LlmAgent agent = agentFactory.budgetAnalyzerAgent(MODEL);
        assertEquals("BudgetAnalyzerAgent", agent.name());
        assertNotNull(agent.description());
    }

    @Test
    void tripPlannerAgent_isCreatedWithSubAgents() {
        LlmAgent hotelAgent = agentFactory.hotelSearchAgent(MODEL);
        LlmAgent tourGuideAgent = agentFactory.tourGuideSearchAgent(MODEL);
        LlmAgent vehicleAgent = agentFactory.vehicleSearchAgent(MODEL);
        LlmAgent itineraryAgent = agentFactory.itineraryGeneratorAgent(MODEL);
        LlmAgent budgetAgent = agentFactory.budgetAnalyzerAgent(MODEL);

        BaseAgent rootAgent = agentFactory.tripPlannerAgent(
                MODEL, hotelAgent, tourGuideAgent, vehicleAgent, itineraryAgent, budgetAgent);

        assertNotNull(rootAgent);
        assertEquals("TripPlannerAgent", rootAgent.name());
    }

    @Test
    void allAgents_useCorrectModel() {
        LlmAgent hotel = agentFactory.hotelSearchAgent(MODEL);
        LlmAgent tourGuide = agentFactory.tourGuideSearchAgent(MODEL);
        LlmAgent vehicle = agentFactory.vehicleSearchAgent(MODEL);
        LlmAgent itinerary = agentFactory.itineraryGeneratorAgent(MODEL);
        LlmAgent budget = agentFactory.budgetAnalyzerAgent(MODEL);

        // All agents should be created without errors
        assertNotNull(hotel);
        assertNotNull(tourGuide);
        assertNotNull(vehicle);
        assertNotNull(itinerary);
        assertNotNull(budget);
    }
}

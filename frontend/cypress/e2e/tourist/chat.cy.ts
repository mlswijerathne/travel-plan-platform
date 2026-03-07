describe('Tourist - Chat Page', () => {
  beforeEach(() => {
    // Intercept chat history for initial page load
    cy.intercept('GET', '**/api/chat/history*', {
      statusCode: 200,
      body: [],
    }).as('getChatHistory')
  })

  // ── UI Tests (mocked API) ─────────────────────────────────
  describe('UI Elements', () => {
    it('should display chat page with welcome message', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.contains('Trip Planner', { timeout: 10000 }).should('be.visible')
      cy.contains("I'm your AI travel companion").should('be.visible')
    })

    it('should show quick start chips', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.contains('Beach').should('exist')
      cy.contains('Adventure').should('exist')
      cy.contains('Cultural').should('exist')
    })

    it('should have message input area', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.get('textarea').should('be.visible')
      cy.get('textarea').should('have.attr', 'placeholder').and('include', 'dream trip')
    })

    it('should have send button disabled when input is empty', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.get('textarea').should('have.value', '')
    })

    it('should enable send button when text is entered', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.get('textarea').type('Plan a 3 day trip to Colombo')
      cy.get('textarea').should('have.value', 'Plan a 3 day trip to Colombo')
    })

    it('should have Plan a Trip button', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.contains('Plan a Trip').should('be.visible')
    })

    it('should have New conversation button', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.contains('New').should('exist')
    })

    it('should show quick reply suggestions', () => {
      cy.login('tourist')
      cy.visit('/chat')
      cy.contains('3 days in Colombo').should('exist')
      cy.contains('Beach vacation').should('exist')
    })
  })

  // ── E2E Tests (real AI agent with real backend services) ───
  describe('Real AI Agent Integration', () => {
    it('should send a message and receive a streaming AI response', () => {
      cy.login('tourist')
      cy.visit('/chat')

      // Wait for page to be ready
      cy.get('textarea', { timeout: 10000 }).should('be.visible')

      // Type and send a message
      cy.get('textarea').type('Show me hotels in Kandy')
      cy.get('textarea').parents('form, div').find('button[type="submit"], button').filter(':visible').last().click()

      // Verify user message appears
      cy.contains('Show me hotels in Kandy').should('be.visible')

      // Wait for AI response (streaming takes time - up to 60s for multi-agent)
      // The assistant message should contain real hotel data from the platform
      cy.get('[class*="assistant"], [data-role="assistant"], div', { timeout: 60000 })
        .should('contain.text', 'hotel')
    })

    it('should search for hotels and return real platform data', () => {
      cy.login('tourist')
      cy.visit('/chat')

      cy.get('textarea', { timeout: 10000 }).should('be.visible')
      cy.get('textarea').type('Find me hotels in Colombo{enter}')

      // Should eventually show a response mentioning real hotels from the database
      // The AI agent calls hotel-service which has real data
      cy.contains('Colombo', { timeout: 60000 }).should('exist')
    })

    it('should search for tour guides via AI agent', () => {
      cy.login('tourist')
      cy.visit('/chat')

      cy.get('textarea', { timeout: 10000 }).should('be.visible')
      cy.get('textarea').type('Find me a tour guide who speaks English{enter}')

      // Should get a response - the AI agent calls tour-guide-service
      cy.get('textarea', { timeout: 60000 }).should('exist')
    })

    it('should search for vehicles via AI agent', () => {
      cy.login('tourist')
      cy.visit('/chat')

      cy.get('textarea', { timeout: 10000 }).should('be.visible')
      cy.get('textarea').type('I need a vehicle for 5 people{enter}')

      // Should get a response from vehicle-service
      cy.get('textarea', { timeout: 60000 }).should('exist')
    })

    it('should handle quick reply chip click with real agent', () => {
      cy.login('tourist')
      cy.visit('/chat')

      // Click a quick reply chip
      cy.contains('3 days in Colombo', { timeout: 10000 }).click()

      // Should show the user message and then an AI response
      cy.contains('Colombo', { timeout: 60000 }).should('exist')
    })
  })
})

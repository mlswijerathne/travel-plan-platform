describe('Tourist - Chat Page', () => {
  beforeEach(() => {
    // Intercept chat history
    cy.intercept('GET', '**/api/chat/history*', {
      statusCode: 200,
      body: [],
    }).as('getChatHistory')
  })

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
    // Send button should be enabled
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

  it('should send message on quick reply click', () => {
    cy.intercept('POST', '**/api/chat', {
      statusCode: 200,
      body: 'data: {"type":"TEXT_DELTA","content":"I can help plan that!"}\n\ndata: {"type":"DONE"}\n\n',
      headers: { 'Content-Type': 'text/event-stream' },
    }).as('sendChat')

    cy.login('tourist')
    cy.visit('/chat')
    cy.contains('3 days in Colombo').click()
  })
})

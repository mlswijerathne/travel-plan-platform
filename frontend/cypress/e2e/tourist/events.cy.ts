describe('Tourist - Events Page', () => {
  beforeEach(() => {
    cy.fixture('events').then((data) => {
      cy.intercept('GET', '**/api/public/events?*', {
        statusCode: 200,
        body: data.eventList,
      }).as('getEvents')

      cy.intercept('GET', '**/api/events?*', {
        statusCode: 200,
        body: data.eventList,
      }).as('getEventsAuth')
    })

    cy.login('tourist')
    cy.visit('/events')
  })

  it('should display the events page heading', () => {
    cy.contains('Events').should('be.visible')
    cy.contains('Discover cultural events').should('be.visible')
  })

  it('should display event cards', () => {
    cy.contains('Kandy Esala Perahera', { timeout: 10000 }).should('be.visible')
    cy.contains('Colombo Food Festival').should('be.visible')
  })

  it('should show event details on cards', () => {
    cy.contains('Kandy').should('be.visible')
    cy.contains('Colombo').should('be.visible')
    cy.contains('$25').should('exist')
    cy.contains('$15').should('exist')
  })

  it('should have search and filter controls', () => {
    cy.get('input').should('exist') // Location search
    cy.contains('Search').should('be.visible')
  })

  it('should navigate to event detail on click', () => {
    cy.contains('Kandy Esala Perahera').click()
    cy.url().should('include', '/events/')
  })

  it('should show empty state when no events found', () => {
    cy.intercept('GET', '**/api/public/events?*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 12, totalElements: 0, totalPages: 0, last: true },
    }).as('getEmptyEvents')

    cy.login('tourist')
    cy.visit('/events')
    cy.wait('@getEmptyEvents')
    cy.get('body').should('contain.text', 'No')
  })
})

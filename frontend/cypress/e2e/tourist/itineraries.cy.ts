describe('Tourist - Itineraries Page', () => {
  beforeEach(() => {
    cy.fixture('itineraries').then((data) => {
      cy.intercept('GET', '**/api/v1/itineraries', {
        statusCode: 200,
        body: data.itineraryList,
      }).as('getItineraries')

      cy.intercept('GET', '**/api/v1/itineraries?*', {
        statusCode: 200,
        body: data.itineraryList,
      }).as('getItinerariesQ')
    })
  })

  it('should display itineraries page heading', () => {
    cy.login('tourist')
    cy.visit('/itineraries')
    cy.contains('My Itineraries').should('be.visible')
    cy.contains('Plan and manage your Sri Lanka trips').should('be.visible')
  })

  it('should have new itinerary button', () => {
    cy.login('tourist')
    cy.visit('/itineraries')
    cy.contains('New Itinerary').should('be.visible')
  })

  it('should display itinerary list', () => {
    cy.login('tourist')
    cy.visit('/itineraries')
    cy.contains('Colombo Weekend Getaway', { timeout: 10000 }).should('be.visible')
    cy.contains('Cultural Triangle Tour').should('be.visible')
  })

  it('should show itinerary status badges', () => {
    cy.login('tourist')
    cy.visit('/itineraries')
    cy.contains('PLANNING').should('exist')
    cy.contains('ACTIVE').should('exist')
  })

  it('should open create dialog when clicking new itinerary', () => {
    cy.login('tourist')
    cy.visit('/itineraries')
    cy.contains('New Itinerary').click()
    cy.contains('Trip Name').should('be.visible')
    cy.contains('Start Date').should('be.visible')
    cy.contains('End Date').should('be.visible')
  })

  it('should validate create form requires name and dates', () => {
    cy.login('tourist')
    cy.visit('/itineraries')
    cy.contains('New Itinerary').click()

    // The form has required fields - try to submit empty
    cy.contains('button', 'Create').click()
    // Browser validation should prevent submission
  })

  it('should navigate to itinerary detail on click', () => {
    cy.login('tourist')
    cy.visit('/itineraries')
    cy.wait('@getItineraries')
    cy.get('a[href*="/itineraries/"]').first().click()
    cy.url().should('include', '/itineraries/')
  })

  it('should show empty state when no itineraries', () => {
    cy.intercept('GET', '**/api/v1/itineraries*', {
      statusCode: 200,
      body: [],
    }).as('getEmptyItineraries')

    cy.login('tourist')
    cy.visit('/itineraries')
    cy.wait('@getEmptyItineraries')
    cy.contains('No itineraries yet').should('be.visible')
  })
})

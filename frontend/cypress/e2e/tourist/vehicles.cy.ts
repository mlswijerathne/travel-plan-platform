describe('Tourist - Vehicles Page', () => {
  beforeEach(() => {
    cy.fixture('vehicles').then((data) => {
      cy.intercept('GET', '**/api/vehicles?*', {
        statusCode: 200,
        body: data.vehicleList,
      }).as('getVehicles')

      cy.intercept('GET', '**/api/vehicles/search*', {
        statusCode: 200,
        body: data.vehicleList,
      }).as('searchVehicles')
    })

    cy.login('tourist')
    cy.visit('/vehicles')
  })

  it('should display the vehicles page heading', () => {
    cy.contains('Find Your Perfect Drive').should('be.visible')
  })

  it('should display vehicle cards', () => {
    cy.contains('Toyota', { timeout: 10000 }).should('be.visible')
    cy.contains('Prius').should('be.visible')
    cy.contains('HiAce').should('be.visible')
  })

  it('should show vehicle details', () => {
    cy.contains('$45').should('exist')
    cy.contains('$85').should('exist')
    cy.contains('CAR').should('exist')
    cy.contains('VAN').should('exist')
  })

  it('should have filter controls', () => {
    // Vehicle type select
    cy.contains('Vehicle Type').should('exist')
  })

  it('should navigate to vehicle detail on click', () => {
    cy.contains('Prius').click()
    cy.url().should('include', '/vehicles/')
  })

  it('should show empty state when no vehicles match', () => {
    cy.intercept('GET', '**/api/vehicles?*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    }).as('getEmptyVehicles')

    cy.login('tourist')
    cy.visit('/vehicles')
    cy.wait('@getEmptyVehicles')
    cy.contains('No vehicles').should('be.visible')
  })
})

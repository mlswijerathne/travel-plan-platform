describe('Provider - Bookings Page', () => {
  beforeEach(() => {
    cy.fixture('bookings').then((data) => {
      cy.intercept('GET', '**/api/bookings/provider/**', {
        statusCode: 200,
        body: data.bookingList,
      }).as('getProviderBookings')
    })

    cy.fixture('hotels').then((data) => {
      cy.intercept('GET', '**/api/hotels/owner*', {
        statusCode: 200,
        body: data.ownerHotels,
      }).as('getOwnerHotels')
    })

    cy.intercept('GET', '**/api/tour-guides/me', {
      statusCode: 200,
      body: { id: 1, firstName: 'Kamal', lastName: 'Perera' },
    }).as('getGuideProfile')
  })

  it('should display provider bookings heading', () => {
    cy.login('hotel_owner')
    cy.visit('/provider/bookings')
    cy.contains('Provider Bookings').should('be.visible')
  })

  it('should have status filter buttons', () => {
    cy.login('hotel_owner')
    cy.visit('/provider/bookings')
    // Status filter buttons should exist
    cy.get('button').should('have.length.at.least', 2)
  })

  it('should display booking cards', () => {
    cy.login('hotel_owner')
    cy.visit('/provider/bookings')
    cy.wait('@getProviderBookings')
    cy.contains('BK-2026-001').should('be.visible')
    cy.contains('CONFIRMED').should('be.visible')
  })

  it('should show empty state when no bookings', () => {
    cy.intercept('GET', '**/api/bookings/provider/**', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    })

    cy.login('hotel_owner')
    cy.visit('/provider/bookings')
    cy.get('body').should('not.contain', 'BK-2026')
  })
})

describe('Tourist - Bookings Page', () => {
  beforeEach(() => {
    cy.fixture('bookings').then((data) => {
      cy.intercept('GET', '**/api/bookings?*', {
        statusCode: 200,
        body: data.bookingList,
      }).as('getBookings')
    })
  })

  it('should display bookings page heading', () => {
    cy.login('tourist')
    cy.visit('/bookings')
    cy.contains('My Bookings').should('be.visible')
    cy.contains('View and manage your travel bookings').should('be.visible')
  })

  it('should have new booking button', () => {
    cy.login('tourist')
    cy.visit('/bookings')
    cy.contains('New Booking').should('be.visible')
  })

  it('should display booking list', () => {
    cy.login('tourist')
    cy.visit('/bookings')
    cy.wait('@getBookings')
    cy.contains('BK-2026-001').should('be.visible')
    cy.contains('Grand Colombo Hotel').should('be.visible')
    cy.contains('CONFIRMED').should('be.visible')
  })

  it('should show status filter buttons', () => {
    cy.login('tourist')
    cy.visit('/bookings')
    cy.contains('button', 'All').should('exist')
  })

  it('should show empty state when no bookings', () => {
    cy.intercept('GET', '**/api/bookings?*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    }).as('getEmptyBookings')

    cy.login('tourist')
    cy.visit('/bookings')
    cy.wait('@getEmptyBookings')
    cy.get('body').should('not.contain', 'BK-2026')
  })

  it('should navigate to booking detail on click', () => {
    cy.login('tourist')
    cy.visit('/bookings')
    cy.wait('@getBookings')
    cy.contains('BK-2026-001').click()
    cy.url().should('include', '/bookings/')
  })
})

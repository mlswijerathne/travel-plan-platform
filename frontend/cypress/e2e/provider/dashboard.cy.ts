describe('Provider - Dashboard', () => {
  describe('Hotel Owner Dashboard', () => {
    beforeEach(() => {
      cy.fixture('hotels').then((data) => {
        cy.intercept('GET', '**/api/hotels/owner*', {
          statusCode: 200,
          body: data.ownerHotels,
        }).as('getOwnerHotels')
      })

      cy.intercept('GET', '**/api/reviews/entity/**', {
        statusCode: 200,
        body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
      }).as('getReviews')
    })

    it('should display dashboard heading', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.contains('Dashboard').should('be.visible')
    })

    it('should show hotel stats cards', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.wait('@getOwnerHotels')
      cy.contains('Hotels').should('exist')
    })

    it('should display hotel list on dashboard', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.wait('@getOwnerHotels')
      cy.contains('Grand Colombo Hotel').should('be.visible')
    })

    it('should have View All link', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.wait('@getOwnerHotels')
      cy.contains('View All').should('be.visible')
    })

    it('should navigate to hotels page from View All', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.wait('@getOwnerHotels')
      cy.contains('View All').click()
      cy.url().should('include', '/provider/hotels')
    })
  })

  describe('Empty Dashboard', () => {
    it('should show empty state for hotel owner with no hotels', () => {
      cy.intercept('GET', '**/api/hotels/owner*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
      }).as('getEmptyHotels')

      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.wait('@getEmptyHotels')
      cy.contains('No hotels').should('exist')
    })
  })
})

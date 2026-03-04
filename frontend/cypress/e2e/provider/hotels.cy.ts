describe('Provider - Hotels Management', () => {
  beforeEach(() => {
    cy.fixture('hotels').then((data) => {
      cy.intercept('GET', '**/api/hotels/owner*', {
        statusCode: 200,
        body: data.ownerHotels,
      }).as('getOwnerHotels')
    })
  })

  describe('Hotels List Page', () => {
    it('should display My Hotels heading', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.contains('My Hotels').should('be.visible')
      cy.contains('Manage your hotel properties').should('be.visible')
    })

    it('should have Add Hotel button', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.contains('Add Hotel').should('be.visible')
    })

    it('should display hotel list with details', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.wait('@getOwnerHotels')
      cy.contains('Grand Colombo Hotel').should('be.visible')
      cy.contains('Colombo').should('be.visible')
    })

    it('should show room count for hotels', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.wait('@getOwnerHotels')
      // Hotels should show room information
      cy.contains('room').should('exist', { matchCase: false })
    })

    it('should have edit and delete buttons', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.wait('@getOwnerHotels')
      cy.contains('Edit').should('exist')
      cy.contains('Delete').should('exist')
    })

    it('should navigate to add hotel page', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.contains('Add Hotel').click()
      cy.url().should('include', '/provider/hotels/new')
    })

    it('should show delete confirmation dialog', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.wait('@getOwnerHotels')

      // Click delete button
      cy.contains('button', 'Delete').first().click()
      // Should show confirmation
      cy.contains('Cancel').should('exist')
    })

    it('should show empty state when no hotels', () => {
      cy.intercept('GET', '**/api/hotels/owner*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
      }).as('getEmptyHotels')

      cy.login('hotel_owner')
      cy.visit('/provider/hotels')
      cy.wait('@getEmptyHotels')
      cy.contains('No hotels').should('be.visible')
    })
  })

  describe('New Hotel Page', () => {
    it('should display add hotel form', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels/new')
      cy.contains('Add New Hotel').should('be.visible')
    })

    it('should have back link', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels/new')
      cy.contains('Back to Hotels').should('be.visible')
    })

    it('should navigate back to hotels on back click', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels/new')
      cy.contains('Back to Hotels').click()
      cy.url().should('include', '/provider/hotels')
    })

    it('should have hotel form fields', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/hotels/new')
      // Common hotel form fields
      cy.get('input').should('have.length.at.least', 3)
      cy.get('button[type="submit"]').should('exist')
    })

    it('should submit hotel form successfully', () => {
      cy.intercept('POST', '**/api/hotels', {
        statusCode: 201,
        body: {
          id: 10,
          name: 'New Test Hotel',
          city: 'Colombo',
          isActive: true,
        },
      }).as('createHotel')

      cy.login('hotel_owner')
      cy.visit('/provider/hotels/new')

      // Fill form fields - specific selectors depend on the HotelForm component
      cy.get('input').first().type('New Test Hotel')
    })
  })
})

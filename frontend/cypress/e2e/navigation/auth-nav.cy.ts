describe('Authenticated Navigation', () => {
  describe('Tourist Navigation', () => {
    beforeEach(() => {
      // Stub API calls to prevent errors
      cy.intercept('GET', '**/api/tourists/me', {
        statusCode: 200,
        body: { id: 1, firstName: 'John', lastName: 'Doe', email: 'tourist@test.com' },
      })
      cy.intercept('GET', '**/api/tourists/me/preferences', {
        statusCode: 200,
        body: { preferredBudget: 'MODERATE' },
      })
    })

    it('should display tourist nav items', () => {
      cy.login('tourist')
      cy.visit('/profile')
      cy.contains('Plan Trip').should('exist')
      cy.contains('Hotels').should('exist')
      cy.contains('Guides').should('exist')
      cy.contains('Vehicles').should('exist')
      cy.contains('Events').should('exist')
      cy.contains('Shop').should('exist')
      cy.contains('Itineraries').should('exist')
      cy.contains('Bookings').should('exist')
      cy.contains('Reviews').should('exist')
    })

    it('should highlight active nav item', () => {
      cy.login('tourist')
      cy.visit('/profile')
      // Profile link should have active styling
      cy.contains('a', 'Profile').should('have.class', 'bg-primary/10')
    })

    it('should have user avatar with dropdown', () => {
      cy.login('tourist')
      cy.visit('/profile')
      // Avatar button on desktop
      cy.get('button').filter('.rounded-full').should('exist')
    })

    it('should navigate between tourist pages', () => {
      cy.intercept('GET', '**/api/hotels?*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 9, totalElements: 0, totalPages: 0, last: true },
      })

      cy.login('tourist')
      cy.visit('/profile')
      cy.contains('a', 'Hotels').click()
      cy.url().should('include', '/hotels')
    })

    it('should show sign out in dropdown', () => {
      cy.login('tourist')
      cy.visit('/profile')
      cy.get('button').filter('.rounded-full').first().click()
      cy.contains('Sign Out').should('be.visible')
    })

    it('should show profile and wallet in dropdown for tourist', () => {
      cy.login('tourist')
      cy.visit('/profile')
      cy.get('button').filter('.rounded-full').first().click()
      cy.contains('Profile').should('be.visible')
      cy.contains('Wallet').should('be.visible')
    })
  })

  describe('Mobile Navigation', () => {
    beforeEach(() => {
      cy.viewport(375, 667) // Mobile viewport

      cy.intercept('GET', '**/api/tourists/me', {
        statusCode: 200,
        body: { id: 1, firstName: 'John', lastName: 'Doe', email: 'tourist@test.com' },
      })
    })

    it('should show hamburger menu on mobile', () => {
      cy.login('tourist')
      cy.visit('/profile')
      // Mobile menu button should be visible
      cy.get('button').filter(':visible').should('have.length.at.least', 1)
    })

    it('should open mobile sheet menu', () => {
      cy.login('tourist')
      cy.visit('/profile')
      // Click the hamburger/menu button (the visible one on mobile)
      cy.get('button').filter(':visible').last().click()
      // Sheet should open - look within the sheet overlay for nav items
      cy.get('[role="dialog"]').should('be.visible')
      cy.get('[role="dialog"]').contains('Plan Trip').should('be.visible')
      cy.get('[role="dialog"]').contains('Sign Out').should('be.visible')
    })

    it('should close mobile menu on link click', () => {
      cy.intercept('GET', '**/api/hotels?*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 9, totalElements: 0, totalPages: 0, last: true },
      })

      cy.login('tourist')
      cy.visit('/profile')
      cy.get('button').filter(':visible').last().click()
      cy.get('[role="dialog"]').contains('a', 'Hotels').click()
      cy.url().should('include', '/hotels')
    })
  })

  describe('Provider Navigation', () => {
    beforeEach(() => {
      cy.intercept('GET', '**/api/hotels/owner*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
      })
    })

    it('should display hotel owner nav items', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.contains('Dashboard').should('exist')
      cy.contains('My Hotels').should('exist')
      cy.contains('Bookings').should('exist')
      cy.contains('Reviews').should('exist')
    })

    it('should show dashboard in dropdown for provider', () => {
      cy.login('hotel_owner')
      cy.visit('/provider/dashboard')
      cy.get('button').filter('.rounded-full').first().click()
      cy.contains('Dashboard').should('be.visible')
    })
  })
})

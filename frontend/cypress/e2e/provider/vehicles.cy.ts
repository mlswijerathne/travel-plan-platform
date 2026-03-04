describe('Provider - Vehicles Management', () => {
  beforeEach(() => {
    cy.fixture('vehicles').then((data) => {
      cy.intercept('GET', '**/api/vehicles/owner*', {
        statusCode: 200,
        body: data.vehicleList,
      }).as('getOwnerVehicles')

      cy.intercept('GET', '**/api/vehicles?*owner*', {
        statusCode: 200,
        body: data.vehicleList,
      }).as('getOwnerVehiclesQ')
    })
  })

  describe('Vehicles List Page', () => {
    it('should display My Vehicles heading', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('My Vehicles').should('be.visible')
      cy.contains('Manage your vehicle fleet').should('be.visible')
    })

    it('should have Add Vehicle button', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('Add Vehicle').should('be.visible')
    })

    it('should display vehicle list', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('Toyota', { timeout: 10000 }).should('be.visible')
      cy.contains('Prius').should('be.visible')
    })

    it('should show vehicle type and capacity', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('CAR').should('exist')
      cy.contains('VAN').should('exist')
    })

    it('should show availability badge', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('Available').should('exist', { matchCase: false })
    })

    it('should show daily rate', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('$45').should('exist')
      cy.contains('$85').should('exist')
    })

    it('should have edit and delete buttons', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('Edit').should('exist')
    })

    it('should navigate to add vehicle page', () => {
      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('Add Vehicle').click()
      cy.url().should('include', '/provider/vehicles/new')
    })

    it('should show empty state when no vehicles', () => {
      cy.intercept('GET', '**/api/vehicles/owner*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
      })
      cy.intercept('GET', '**/api/vehicles?*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
      })

      cy.login('vehicle_owner')
      cy.visit('/provider/vehicles')
      cy.contains('No vehicles').should('be.visible')
    })
  })
})

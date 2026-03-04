describe('Admin - Dashboard', () => {
  beforeEach(() => {
    cy.fixture('packages').then((data) => {
      cy.intercept('GET', '**/api/packages*', {
        statusCode: 200,
        body: data.packageList,
      }).as('getPackages')
    })

    cy.fixture('events').then((data) => {
      cy.intercept('GET', '**/api/events/organizer/my*', {
        statusCode: 200,
        body: data.eventList,
      }).as('getAdminEvents')

      cy.intercept('GET', '**/api/events?*', {
        statusCode: 200,
        body: data.eventList,
      }).as('getEvents')
    })
  })

  it('should display admin dashboard heading', () => {
    cy.login('admin')
    cy.visit('/admin')
    cy.contains('Admin Dashboard').should('be.visible')
    cy.contains('Manage packages, events, and platform content').should('be.visible')
  })

  it('should show stats cards', () => {
    cy.login('admin')
    cy.visit('/admin')
    cy.contains('Packages').should('exist')
    cy.contains('Events').should('exist')
  })

  it('should show recent packages section', () => {
    cy.login('admin')
    cy.visit('/admin')
    cy.wait('@getPackages')
    cy.contains('Sri Lanka Classic Tour').should('be.visible')
  })

  it('should have New Package button', () => {
    cy.login('admin')
    cy.visit('/admin')
    cy.contains('New').should('exist')
  })

  it('should have View all links', () => {
    cy.login('admin')
    cy.visit('/admin')
    cy.contains('View all').should('exist')
  })

  it('should navigate to packages page', () => {
    cy.login('admin')
    cy.visit('/admin')
    cy.contains('a', 'View all').first().click()
    cy.url().should('match', /\/admin\/(packages|events)/)
  })
})

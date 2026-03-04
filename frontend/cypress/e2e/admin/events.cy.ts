describe('Admin - Events Management', () => {
  beforeEach(() => {
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

  it('should display events heading', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('Events').should('be.visible')
  })

  it('should have New Event button', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('New Event').should('be.visible')
  })

  it('should have status filter', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.get('select, button').should('have.length.at.least', 1)
  })

  it('should display event list', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('Kandy Esala Perahera', { timeout: 10000 }).should('be.visible')
    cy.contains('Colombo Food Festival').should('be.visible')
  })

  it('should show event details', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('Kandy').should('be.visible')
    cy.contains('$25').should('exist')
    cy.contains('PUBLISHED').should('exist')
  })

  it('should have edit and delete buttons', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('Edit').should('exist')
    cy.contains('Delete').should('exist')
  })

  it('should navigate to new event page', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('New Event').click()
    cy.url().should('include', '/admin/events/new')
  })

  it('should show delete confirmation', () => {
    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('Delete').first().click()
    cy.contains('Cancel').should('exist')
  })

  it('should show empty state when no events', () => {
    cy.intercept('GET', '**/api/events/organizer/my*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 12, totalElements: 0, totalPages: 0, last: true },
    })
    cy.intercept('GET', '**/api/events?*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 12, totalElements: 0, totalPages: 0, last: true },
    })

    cy.login('admin')
    cy.visit('/admin/events')
    cy.contains('No events').should('be.visible')
  })
})

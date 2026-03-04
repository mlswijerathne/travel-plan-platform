describe('Admin - Packages Management', () => {
  beforeEach(() => {
    cy.fixture('packages').then((data) => {
      cy.intercept('GET', '**/api/packages*', {
        statusCode: 200,
        body: data.packageList,
      }).as('getPackages')
    })
  })

  it('should display packages heading', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.contains('Packages').should('be.visible')
  })

  it('should have New Package button', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.contains('New Package').should('be.visible')
  })

  it('should display package list', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('Sri Lanka Classic Tour').should('be.visible')
    cy.contains('Beach Paradise Package').should('be.visible')
  })

  it('should show package details', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('7').should('exist') // duration
    cy.contains('$899').should('exist')
  })

  it('should show Featured badge for featured packages', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('Featured').should('exist')
  })

  it('should show discounted price', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('$899').should('exist')
    cy.contains('1099').should('exist')
  })

  it('should have edit and delete buttons', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('Edit').should('exist')
    cy.contains('Delete').should('exist')
  })

  it('should navigate to new package page', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.contains('New Package').click()
    cy.url().should('include', '/admin/packages/new')
  })

  it('should navigate to edit page', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('Edit').first().click()
    cy.url().should('include', '/admin/packages/')
    cy.url().should('include', '/edit')
  })

  it('should show delete confirmation', () => {
    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('Delete').first().click()
    cy.contains('Cancel').should('exist')
  })

  it('should delete package on confirm', () => {
    cy.intercept('DELETE', '**/api/packages/*', {
      statusCode: 204,
    }).as('deletePackage')

    cy.login('admin')
    cy.visit('/admin/packages')
    cy.wait('@getPackages')
    cy.contains('Delete').first().click()
    // Confirm deletion
    cy.get('button').contains('Delete').last().click()
    cy.wait('@deletePackage')
  })

  it('should show empty state when no packages', () => {
    cy.intercept('GET', '**/api/packages*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    })

    cy.login('admin')
    cy.visit('/admin/packages')
    cy.contains('No packages').should('be.visible')
  })
})

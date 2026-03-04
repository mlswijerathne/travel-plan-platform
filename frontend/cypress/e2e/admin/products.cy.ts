describe('Admin - Products Management', () => {
  beforeEach(() => {
    cy.fixture('products').then((data) => {
      cy.intercept('GET', '**/api/products*', {
        statusCode: 200,
        body: data.productList,
      }).as('getProducts')
    })
  })

  it('should display products heading', () => {
    cy.login('admin')
    cy.visit('/admin/products')
    cy.contains('Products').should('be.visible')
    cy.contains('Manage shop products').should('be.visible')
  })

  it('should display products table', () => {
    cy.login('admin')
    cy.visit('/admin/products')
    cy.wait('@getProducts')
    cy.contains('Ceylon Tea Gift Box').should('be.visible')
    cy.contains('Travel Backpack').should('be.visible')
  })

  it('should show product categories', () => {
    cy.login('admin')
    cy.visit('/admin/products')
    cy.wait('@getProducts')
    cy.contains('SOUVENIRS').should('exist')
    cy.contains('GEAR').should('exist')
  })

  it('should show product prices', () => {
    cy.login('admin')
    cy.visit('/admin/products')
    cy.wait('@getProducts')
    cy.contains('$35').should('exist')
    cy.contains('$89').should('exist')
  })

  it('should show stock quantities', () => {
    cy.login('admin')
    cy.visit('/admin/products')
    cy.wait('@getProducts')
    cy.contains('50').should('exist')
  })

  it('should show active/inactive status', () => {
    cy.login('admin')
    cy.visit('/admin/products')
    cy.wait('@getProducts')
    cy.contains('Active').should('exist')
  })

  it('should have delete button for each product', () => {
    cy.login('admin')
    cy.visit('/admin/products')
    cy.wait('@getProducts')
    cy.contains('Delete').should('exist')
  })

  it('should delete product', () => {
    cy.intercept('DELETE', '**/api/products/*', {
      statusCode: 204,
    }).as('deleteProduct')

    cy.login('admin')
    cy.visit('/admin/products')
    cy.wait('@getProducts')
    cy.contains('button', 'Delete').first().click()
  })

  it('should show empty state when no products', () => {
    cy.intercept('GET', '**/api/products*', {
      statusCode: 200,
      body: [],
    })

    cy.login('admin')
    cy.visit('/admin/products')
    cy.contains('No products').should('be.visible')
  })
})

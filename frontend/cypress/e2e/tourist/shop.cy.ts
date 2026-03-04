describe('Tourist - Shop Page', () => {
  beforeEach(() => {
    cy.fixture('products').then((data) => {
      cy.intercept('GET', '**/api/products*', {
        statusCode: 200,
        body: data.productList,
      }).as('getProducts')
    })

    cy.login('tourist')
    cy.visit('/shop')
  })

  it('should display the shop page heading', () => {
    cy.contains('Travel Shop').should('be.visible')
    cy.contains('Souvenirs, Gear & Essentials').should('be.visible')
  })

  it('should have add product button', () => {
    cy.contains('List New Product').should('be.visible')
  })

  it('should display product cards', () => {
    cy.wait('@getProducts')
    cy.contains('Ceylon Tea Gift Box').should('be.visible')
    cy.contains('Travel Backpack').should('be.visible')
  })

  it('should show product prices', () => {
    cy.wait('@getProducts')
    cy.contains('Rs. 35').should('exist')
    cy.contains('Rs. 89').should('exist')
  })

  it('should show stock information', () => {
    cy.wait('@getProducts')
    cy.get('body').should('contain.text', '50')
    cy.contains('Out of Stock').should('exist')
  })

  it('should show category badges', () => {
    cy.wait('@getProducts')
    cy.contains('SOUVENIRS').should('exist')
    cy.contains('GEAR').should('exist')
  })

  it('should disable buy button for out of stock items', () => {
    cy.wait('@getProducts')
    // Product with stockQuantity: 0 should have disabled buy button
    cy.contains('Out of Stock').should('exist')
  })

  it('should navigate to buy page for in-stock items', () => {
    cy.wait('@getProducts')
    cy.contains('Buy Now').first().click()
    cy.url().should('include', '/shop/buy/')
  })

  it('should show empty state when no products', () => {
    cy.intercept('GET', '**/api/products*', {
      statusCode: 200,
      body: [],
    }).as('getEmptyProducts')

    cy.login('tourist')
    cy.visit('/shop')
    cy.wait('@getEmptyProducts')
    cy.contains('No items').should('be.visible')
  })

  it('should show loading state', () => {
    cy.intercept('GET', '**/api/products*', {
      statusCode: 200,
      body: [],
      delay: 2000,
    })

    cy.login('tourist')
    cy.visit('/shop')
    cy.contains('Loading').should('exist')
  })
})

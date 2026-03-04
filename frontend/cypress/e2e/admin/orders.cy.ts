describe('Admin - Orders Management', () => {
  beforeEach(() => {
    cy.fixture('orders').then((data) => {
      cy.intercept('GET', '**/api/orders/admin*', {
        statusCode: 200,
        body: data.orderList,
      }).as('getAdminOrders')

      cy.intercept('GET', '**/api/orders?*', {
        statusCode: 200,
        body: data.orderList,
      }).as('getOrders')
    })
  })

  it('should display orders heading', () => {
    cy.login('admin')
    cy.visit('/admin/orders')
    cy.contains('Orders').should('be.visible')
    cy.contains('Manage customer orders').should('be.visible')
  })

  it('should have status filter', () => {
    cy.login('admin')
    cy.visit('/admin/orders')
    cy.get('select, button').should('have.length.at.least', 1)
  })

  it('should display order table', () => {
    cy.login('admin')
    cy.visit('/admin/orders')
    cy.contains('ORD-001', { timeout: 10000 }).should('be.visible')
  })

  it('should show order details', () => {
    cy.login('admin')
    cy.visit('/admin/orders')
    cy.contains('$70').should('exist')
    cy.contains('PENDING').should('exist')
  })

  it('should have status update dropdown', () => {
    cy.login('admin')
    cy.visit('/admin/orders')
    cy.get('select').should('have.length.at.least', 1)
  })

  it('should update order status', () => {
    cy.intercept('PUT', '**/api/orders/*/status', {
      statusCode: 200,
      body: { success: true },
    }).as('updateStatus')

    cy.login('admin')
    cy.visit('/admin/orders')
    // Select new status from dropdown
    cy.get('select').last().select('CONFIRMED')
    cy.wait('@updateStatus')
  })

  it('should show empty state when no orders', () => {
    cy.intercept('GET', '**/api/orders/admin*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    })
    cy.intercept('GET', '**/api/orders?*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    })

    cy.login('admin')
    cy.visit('/admin/orders')
    cy.contains('No orders').should('be.visible')
  })
})

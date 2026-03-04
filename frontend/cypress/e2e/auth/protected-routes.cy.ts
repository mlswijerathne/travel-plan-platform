describe('Protected Routes', () => {
  describe('Unauthenticated user', () => {
    it('should redirect /provider/* to /login', () => {
      cy.visit('/provider/dashboard')
      cy.url().should('include', '/login')
    })

    it('should redirect /provider/hotels to /login', () => {
      cy.visit('/provider/hotels')
      cy.url().should('include', '/login')
    })

    it('should redirect /provider/bookings to /login', () => {
      cy.visit('/provider/bookings')
      cy.url().should('include', '/login')
    })

    it('should redirect /admin to /login', () => {
      cy.visit('/admin')
      cy.url().should('include', '/login')
    })

    it('should redirect /admin/packages to /login', () => {
      cy.visit('/admin/packages')
      cy.url().should('include', '/login')
    })

    it('should redirect /admin/events to /login', () => {
      cy.visit('/admin/events')
      cy.url().should('include', '/login')
    })
  })

  describe('Public routes accessible without auth', () => {
    it('should load landing page', () => {
      cy.visit('/')
      cy.contains('Sri Lanka').should('be.visible')
    })

    it('should load login page', () => {
      cy.visit('/login')
      cy.contains('Welcome Back').should('be.visible')
    })

    it('should load register page', () => {
      cy.visit('/register')
      cy.contains('Create Account').should('be.visible')
    })
  })
})

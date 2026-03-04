describe('Login Page', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('should display the login form', () => {
    cy.contains('h1', 'Welcome Back').should('be.visible')
    cy.contains('Sign in to plan your next adventure').should('be.visible')
    cy.get('input#email').should('be.visible')
    cy.get('input#password').should('be.visible')
    cy.get('button[type="submit"]').should('contain', 'Sign In')
  })

  it('should display the brand panel on desktop', () => {
    cy.contains('Welcome back,').should('exist')
    cy.contains('AI-powered trip planning').should('exist')
    cy.contains('Personalized itineraries').should('exist')
    cy.contains('Verified local providers').should('exist')
  })

  it('should have link to register page', () => {
    cy.contains('a', 'Create one').should('have.attr', 'href', '/register')
  })

  it('should have link to forgot password', () => {
    cy.contains('button', 'Forgot password?').should('be.visible')
  })

  it('should require email and password fields', () => {
    cy.get('input#email').should('have.attr', 'required')
    cy.get('input#password').should('have.attr', 'required')
  })

  it('should show error for invalid credentials', () => {
    cy.get('input#email').type('invalid@test.com')
    cy.get('input#password').type('wrongpassword')
    cy.get('button[type="submit"]').click()
    cy.contains('Signing in...').should('be.visible')
    cy.get('[data-sonner-toast]', { timeout: 10000 }).should('be.visible')
  })

  it('should show loading state during submission', () => {
    cy.get('input#email').type('test@test.com')
    cy.get('input#password').type('password123')
    cy.get('button[type="submit"]').click()
    cy.get('button[type="submit"]').should('be.disabled')
    cy.contains('Signing in...').should('be.visible')
  })

  it('should redirect tourist to /profile after login', () => {
    cy.login('tourist')
    cy.url().should('not.include', '/login')
  })

  it('should redirect hotel owner to /provider/dashboard after login', () => {
    cy.login('hotel_owner')
    cy.url().should('not.include', '/login')
  })
})

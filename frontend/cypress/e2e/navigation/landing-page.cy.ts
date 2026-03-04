describe('Landing Page', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('should display the hero section', () => {
    cy.contains('AI-Powered Travel Planning').should('be.visible')
    cy.contains('Your AI Travel Companion for Sri Lanka').should('be.visible')
  })

  it('should have CTA buttons in hero', () => {
    cy.contains('Start Planning Free').should('be.visible')
    cy.contains('Sign In').should('be.visible')
  })

  it('should display stats', () => {
    cy.contains('Hotels').should('exist')
    cy.contains('Guides').should('exist')
    cy.contains('Rating').should('exist')
  })

  it('should display features section', () => {
    cy.contains('Everything you need for the perfect trip').should('be.visible')
    cy.contains('AI Trip Planning').should('exist')
    cy.contains('Smart Itineraries').should('exist')
    cy.contains('Trusted Providers').should('exist')
    cy.contains('Budget Control').should('exist')
  })

  it('should display how it works section', () => {
    cy.contains('Three steps to your dream trip').should('be.visible')
    cy.contains('Tell Us Your Dream').should('exist')
    cy.contains('Get Your Plan').should('exist')
    cy.contains('Book & Go').should('exist')
  })

  it('should display destinations section', () => {
    cy.contains('Popular destinations').should('be.visible')
    cy.contains('Colombo').should('be.visible')
    cy.contains('Kandy').should('be.visible')
    cy.contains('Galle').should('be.visible')
    cy.contains('Ella').should('be.visible')
    cy.contains('Sigiriya').should('be.visible')
    cy.contains('Mirissa').should('be.visible')
  })

  it('should display for providers section', () => {
    cy.contains('Hotel Owners').should('exist')
    cy.contains('Tour Guides').should('exist')
    cy.contains('Vehicle Owners').should('exist')
  })

  it('should display CTA section at bottom', () => {
    cy.contains('Ready to plan your').should('be.visible')
    cy.contains('Sri Lanka adventure').should('be.visible')
    cy.contains('Get Started Free').should('exist')
  })

  it('should have navigation bar with logo', () => {
    cy.contains('TravelPlan').should('be.visible')
  })

  it('should have nav links', () => {
    cy.contains('Features').should('exist')
    cy.contains('How It Works').should('exist')
    cy.contains('Destinations').should('exist')
    cy.contains('For Providers').should('exist')
  })

  it('should navigate to login on Sign In click', () => {
    cy.contains('a', 'Sign In').first().click()
    cy.url().should('include', '/login')
  })

  it('should navigate to register on Get Started click', () => {
    cy.contains('a', 'Start Planning Free').first().click()
    cy.url().should('include', '/register')
  })

  it('should navigate to register with role for provider CTA', () => {
    // Scroll to provider section and click register link
    cy.contains('Hotel Owners').scrollIntoView()
    cy.contains('Hotel Owners').parent().find('a').click()
    cy.url().should('include', '/register')
  })

  it('should have footer with copyright', () => {
    cy.contains('TravelPlan').should('exist')
    cy.contains(new Date().getFullYear().toString()).should('exist')
  })
})

describe('Tourist - Guides Page', () => {
  beforeEach(() => {
    cy.fixture('guides').then((data) => {
      cy.intercept('GET', '**/api/tour-guides?*', {
        statusCode: 200,
        body: data.guideList,
      }).as('getGuides')

      cy.intercept('GET', '**/api/tour-guides/search*', {
        statusCode: 200,
        body: data.guideList,
      }).as('searchGuides')
    })

    cy.login('tourist')
    cy.visit('/guides')
  })

  it('should display the guides page heading', () => {
    cy.contains('Tour Guides').should('be.visible')
    cy.contains('Find experienced local guides').should('be.visible')
  })

  it('should display guide cards', () => {
    cy.wait('@getGuides')
    cy.contains('Kamal Perera').should('be.visible')
    cy.contains('Nimal Silva').should('be.visible')
  })

  it('should show guide specializations', () => {
    cy.wait('@getGuides')
    cy.contains('cultural').should('exist')
    cy.contains('adventure').should('exist')
  })

  it('should show guide rates', () => {
    cy.wait('@getGuides')
    cy.contains('25.00').should('exist')
    cy.contains('150.00').should('exist')
  })

  it('should navigate to guide detail on click', () => {
    cy.wait('@getGuides')
    cy.contains('Kamal Perera').click()
    cy.url().should('include', '/guides/1')
  })

  it('should show empty state when no guides found', () => {
    cy.intercept('GET', '**/api/tour-guides?*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 9, totalElements: 0, totalPages: 0, last: true },
    }).as('getEmptyGuides')

    cy.login('tourist')
    cy.visit('/guides')
    cy.wait('@getEmptyGuides')
    cy.contains('No guides found').should('be.visible')
  })

  it('should show loading state', () => {
    cy.intercept('GET', '**/api/tour-guides?*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 9, totalElements: 0, totalPages: 0, last: true },
      delay: 2000,
    })

    cy.login('tourist')
    cy.visit('/guides')
    cy.get('.animate-pulse').should('exist')
  })
})

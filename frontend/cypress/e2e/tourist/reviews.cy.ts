describe('Tourist - Reviews Page', () => {
  beforeEach(() => {
    cy.fixture('reviews').then((data) => {
      cy.intercept('GET', '**/api/reviews/my*', {
        statusCode: 200,
        body: data.myReviews,
      }).as('getMyReviews')

      cy.intercept('GET', '**/api/reviews/pending*', {
        statusCode: 200,
        body: data.pendingReviews,
      }).as('getPendingReviews')
    })
  })

  it('should display reviews page heading', () => {
    cy.login('tourist')
    cy.visit('/reviews')
    cy.contains('Reviews').should('be.visible')
    cy.contains('Manage your reviews').should('be.visible')
  })

  it('should show My Reviews tab', () => {
    cy.login('tourist')
    cy.visit('/reviews')
    cy.contains('My Reviews').should('be.visible')
  })

  it('should show Pending tab with count', () => {
    cy.login('tourist')
    cy.visit('/reviews')
    cy.contains('Pending').should('be.visible')
  })

  it('should display reviews in My Reviews tab', () => {
    cy.login('tourist')
    cy.visit('/reviews')
    cy.wait('@getMyReviews')
    cy.contains('Amazing stay').should('be.visible')
    cy.contains('Grand Colombo Hotel').should('be.visible')
  })

  it('should show review rating', () => {
    cy.login('tourist')
    cy.visit('/reviews')
    cy.wait('@getMyReviews')
    cy.contains('HOTEL').should('exist')
  })

  it('should switch to Pending tab', () => {
    cy.login('tourist')
    cy.visit('/reviews')
    cy.contains('Pending').click()
    cy.wait('@getPendingReviews')
    cy.contains('Kamal Perera').should('be.visible')
  })

  it('should show empty state when no reviews', () => {
    cy.intercept('GET', '**/api/reviews/my*', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    }).as('getEmptyReviews')

    cy.login('tourist')
    cy.visit('/reviews')
    cy.wait('@getEmptyReviews')
  })
})

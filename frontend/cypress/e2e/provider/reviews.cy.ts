describe('Provider - Reviews Page', () => {
  beforeEach(() => {
    cy.fixture('reviews').then((data) => {
      cy.intercept('GET', '**/api/reviews/entity/**', {
        statusCode: 200,
        body: data.myReviews,
      }).as('getEntityReviews')

      cy.intercept('GET', '**/api/reviews/summary/**', {
        statusCode: 200,
        body: data.reviewSummary,
      }).as('getReviewSummary')
    })

    cy.fixture('hotels').then((data) => {
      cy.intercept('GET', '**/api/hotels/owner*', {
        statusCode: 200,
        body: data.ownerHotels,
      }).as('getOwnerHotels')
    })

    cy.intercept('GET', '**/api/tour-guides/me', {
      statusCode: 200,
      body: { id: 1, firstName: 'Kamal', lastName: 'Perera' },
    }).as('getGuideProfile')
  })

  it('should display reviews heading', () => {
    cy.login('hotel_owner')
    cy.visit('/provider/reviews')
    cy.contains('Reviews').should('be.visible')
  })

  it('should display review list', () => {
    cy.login('hotel_owner')
    cy.visit('/provider/reviews')
    cy.wait('@getEntityReviews')
    cy.contains('Amazing stay').should('be.visible')
  })

  it('should show rating for each review', () => {
    cy.login('hotel_owner')
    cy.visit('/provider/reviews')
    cy.wait('@getEntityReviews')
    cy.contains('HOTEL').should('exist')
  })

  it('should show verified badge', () => {
    cy.login('hotel_owner')
    cy.visit('/provider/reviews')
    cy.wait('@getEntityReviews')
    cy.contains('Verified').should('exist', { matchCase: false })
  })

  it('should show empty state when no reviews', () => {
    cy.intercept('GET', '**/api/reviews/entity/**', {
      statusCode: 200,
      body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
    })

    cy.login('hotel_owner')
    cy.visit('/provider/reviews')
    cy.get('body').should('not.contain', 'Amazing stay')
  })
})

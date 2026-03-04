describe('Tourist - Hotels Page', () => {
  beforeEach(() => {
    cy.fixture('hotels').then((data) => {
      cy.intercept('GET', '**/api/hotels?*', {
        statusCode: 200,
        body: data.hotelList,
      }).as('getHotels')

      cy.intercept('GET', '**/api/hotels/search*', {
        statusCode: 200,
        body: data.hotelList,
      }).as('searchHotels')
    })
  })

  describe('Hotels Listing', () => {
    beforeEach(() => {
      cy.login('tourist')
      cy.visit('/hotels')
      cy.wait('@getHotels')
    })

    it('should display the hotels page heading', () => {
      cy.contains('Hotels').should('be.visible')
      cy.contains('Find your perfect stay in Sri Lanka').should('be.visible')
    })

    it('should display hotel cards', () => {
      cy.contains('Grand Colombo Hotel').should('be.visible')
      cy.contains('Kandy Hills Resort').should('be.visible')
      cy.contains('Galle Beach House').should('be.visible')
    })

    it('should show hotel details on cards', () => {
      cy.contains('Colombo').should('be.visible')
      cy.contains('Kandy').should('be.visible')
      cy.contains('Galle').should('be.visible')
    })

    it('should navigate to hotel detail on click', () => {
      cy.contains('Grand Colombo Hotel').click()
      cy.url().should('include', '/hotels/1')
    })
  })

  describe('Hotels Filters', () => {
    beforeEach(() => {
      cy.login('tourist')
      cy.visit('/hotels')
      cy.wait('@getHotels')
    })

    it('should have filter controls', () => {
      // City filter or search input should exist
      cy.get('input, select').should('have.length.at.least', 1)
    })
  })

  describe('Empty State', () => {
    it('should show empty state when no hotels found', () => {
      cy.intercept('GET', '**/api/hotels?*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 9, totalElements: 0, totalPages: 0, last: true },
      }).as('getEmptyHotels')

      cy.login('tourist')
      cy.visit('/hotels')
      cy.wait('@getEmptyHotels')
      cy.contains('No hotels found').should('be.visible')
    })
  })

  describe('Loading State', () => {
    it('should show loading skeleton while fetching', () => {
      cy.intercept('GET', '**/api/hotels?*', {
        statusCode: 200,
        body: { content: [], page: 0, size: 9, totalElements: 0, totalPages: 0, last: true },
        delay: 2000,
      }).as('getHotelsSlow')

      cy.login('tourist')
      cy.visit('/hotels')
      cy.get('.animate-pulse').should('exist')
    })
  })
})

describe('Tourist - Hotel Detail Page', () => {
  beforeEach(() => {
    cy.fixture('hotels').then((data) => {
      cy.intercept('GET', '**/api/hotels/1/details', {
        statusCode: 200,
        body: data.hotelDetail,
      }).as('getHotelDetail')

      cy.intercept('GET', '**/api/hotels/1', {
        statusCode: 200,
        body: data.hotelDetail,
      }).as('getHotel')

      cy.intercept('GET', '**/api/reviews/entity/**', {
        statusCode: 200,
        body: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true },
      }).as('getReviews')

      cy.intercept('GET', '**/api/reviews/summary/**', {
        statusCode: 200,
        body: { averageRating: 4.5, totalReviews: 120 },
      }).as('getReviewSummary')
    })

    cy.login('tourist')
    cy.visit('/hotels/1')
  })

  it('should display hotel name and details', () => {
    cy.contains('Grand Colombo Hotel', { timeout: 10000 }).should('be.visible')
    cy.contains('Colombo').should('be.visible')
  })

  it('should display amenities', () => {
    cy.contains('Wifi').should('be.visible')
    cy.contains('Pool').should('be.visible')
  })

  it('should display rooms section', () => {
    cy.contains('DELUXE').should('be.visible')
    cy.contains('SUITE').should('be.visible')
    cy.contains('150.00').should('be.visible')
    cy.contains('350.00').should('be.visible')
  })

  it('should have availability check inputs', () => {
    cy.get('input[type="date"]').should('have.length.at.least', 2)
  })

  it('should check availability when dates selected', () => {
    cy.fixture('hotels').then((data) => {
      cy.intercept('GET', '**/api/hotels/1/availability*', {
        statusCode: 200,
        body: data.availability,
      }).as('checkAvailability')
    })

    cy.get('input[type="date"]').first().type('2026-04-01')
    cy.get('input[type="date"]').last().type('2026-04-05')

    cy.wait('@checkAvailability')
    cy.contains('Rooms available for booking').should('be.visible')
  })

  it('should have back to hotels link', () => {
    cy.contains('Back to Hotels').should('be.visible')
    cy.contains('Back to Hotels').click()
    cy.url().should('include', '/hotels')
  })
})

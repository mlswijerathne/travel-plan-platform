describe('Tourist - Profile Page', () => {
  beforeEach(() => {
    cy.fixture('tourist').then((data) => {
      cy.intercept('GET', '**/api/tourists/me', {
        statusCode: 200,
        body: data.profile,
      }).as('getProfile')

      cy.intercept('GET', '**/api/tourists/me/preferences', {
        statusCode: 200,
        body: data.preferences,
      }).as('getPreferences')
    })
  })

  it('should display profile page with user info', () => {
    cy.login('tourist')
    cy.visit('/profile')
    cy.wait('@getProfile')
    cy.contains('John Doe').should('be.visible')
    cy.contains('tourist@test.com').should('be.visible')
  })

  it('should show profile tabs', () => {
    cy.login('tourist')
    cy.visit('/profile')
    cy.contains('Profile').should('be.visible')
    cy.contains('Preferences').should('be.visible')
  })

  it('should display profile form fields', () => {
    cy.login('tourist')
    cy.visit('/profile')
    cy.wait('@getProfile')
    cy.get('input').should('have.length.at.least', 2)
  })

  it('should have save button in profile tab', () => {
    cy.login('tourist')
    cy.visit('/profile')
    cy.wait('@getProfile')
    cy.contains('Save Changes').should('be.visible')
  })

  it('should update profile on save', () => {
    cy.intercept('PUT', '**/api/tourists/me', {
      statusCode: 200,
      body: { success: true },
    }).as('updateProfile')

    cy.login('tourist')
    cy.visit('/profile')
    cy.wait('@getProfile')
    cy.contains('Save Changes').click()
    cy.wait('@updateProfile')
  })

  it('should switch to preferences tab', () => {
    cy.login('tourist')
    cy.visit('/profile')
    cy.contains('Preferences').click()
    cy.contains('Budget Level').should('be.visible')
  })

  it('should show interest badges in preferences', () => {
    cy.login('tourist')
    cy.visit('/profile')
    cy.contains('Preferences').click()
    cy.contains('Adventure').should('exist')
    cy.contains('Culture').should('exist')
    cy.contains('Nature').should('exist')
  })

  it('should save preferences', () => {
    cy.intercept('PUT', '**/api/tourists/me/preferences', {
      statusCode: 200,
      body: { success: true },
    }).as('updatePreferences')

    cy.login('tourist')
    cy.visit('/profile')
    cy.contains('Preferences').click()
    cy.contains('Save Preferences').click()
    cy.wait('@updatePreferences')
  })

  it('should show loading skeleton while fetching', () => {
    cy.intercept('GET', '**/api/tourists/me', {
      statusCode: 200,
      body: {},
      delay: 2000,
    })

    cy.login('tourist')
    cy.visit('/profile')
    cy.get('.animate-pulse').should('exist')
  })
})

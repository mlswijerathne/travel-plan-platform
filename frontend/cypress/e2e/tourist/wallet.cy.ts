describe('Tourist - Wallet Page', () => {
  beforeEach(() => {
    cy.fixture('tourist').then((data) => {
      cy.intercept('GET', '**/api/tourists/me/wallet', {
        statusCode: 200,
        body: data.wallet,
      }).as('getWallet')

      cy.intercept('GET', '**/api/tourists/me', {
        statusCode: 200,
        body: data.profile,
      }).as('getProfile')
    })
  })

  it('should display wallet page', () => {
    cy.login('tourist')
    cy.visit('/wallet')
    cy.contains('Wallet').should('be.visible')
  })

  it('should show wallet balance', () => {
    cy.login('tourist')
    cy.visit('/wallet')
    cy.wait('@getWallet')
    cy.contains('250').should('be.visible')
  })

  it('should show transaction history', () => {
    cy.login('tourist')
    cy.visit('/wallet')
    cy.wait('@getWallet')
    cy.contains('Welcome bonus').should('be.visible')
  })
})

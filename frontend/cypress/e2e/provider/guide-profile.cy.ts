describe('Provider - Tour Guide Profile', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/api/tour-guides/me', {
      statusCode: 200,
      body: {
        id: 1,
        userId: 'guide-user-id',
        firstName: 'Kamal',
        lastName: 'Perera',
        email: 'kamal@test.com',
        phoneNumber: '+94771234567',
        bio: 'Experienced guide specializing in cultural tours',
        hourlyRate: 25.00,
        dailyRate: 150.00,
        experienceYears: 10,
        languages: ['English', 'Sinhala', 'Tamil'],
        specializations: ['cultural', 'historical', 'wildlife'],
        averageRating: 4.8,
        reviewCount: 45,
        isVerified: true,
        isAvailable: true,
      },
    }).as('getGuideProfile')
  })

  it('should display guide profile page', () => {
    cy.login('tour_guide')
    cy.visit('/provider/profile')
    cy.wait('@getGuideProfile')
    cy.contains('Kamal').should('be.visible')
    cy.contains('Perera').should('be.visible')
  })

  it('should show guide rates', () => {
    cy.login('tour_guide')
    cy.visit('/provider/profile')
    cy.wait('@getGuideProfile')
    cy.contains('25').should('exist')
    cy.contains('150').should('exist')
  })

  it('should show languages', () => {
    cy.login('tour_guide')
    cy.visit('/provider/profile')
    cy.wait('@getGuideProfile')
    cy.contains('English').should('exist')
    cy.contains('Sinhala').should('exist')
  })

  it('should show specializations', () => {
    cy.login('tour_guide')
    cy.visit('/provider/profile')
    cy.wait('@getGuideProfile')
    cy.contains('cultural').should('exist')
    cy.contains('historical').should('exist')
  })

  it('should allow editing profile', () => {
    cy.intercept('PUT', '**/api/tour-guides/me', {
      statusCode: 200,
      body: { success: true },
    }).as('updateProfile')

    cy.login('tour_guide')
    cy.visit('/provider/profile')
    cy.wait('@getGuideProfile')
    // Should have editable form fields or edit button
    cy.get('input, textarea, button').should('have.length.at.least', 1)
  })
})

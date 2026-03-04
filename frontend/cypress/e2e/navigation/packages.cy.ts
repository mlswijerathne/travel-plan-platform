describe('Packages Page (Public)', () => {
  const mockPackages = [
    {
      id: 1,
      name: 'Sri Lanka Classic Tour',
      description: '7-day tour covering major attractions',
      durationDays: 7,
      basePrice: 89900.00,
      discountPercentage: 10,
      maxParticipants: 15,
      destinations: ['Colombo', 'Kandy', 'Sigiriya', 'Galle'],
      isFeatured: true,
      isActive: true,
    },
    {
      id: 2,
      name: 'Beach Paradise Package',
      description: '5-day beach vacation',
      durationDays: 5,
      basePrice: 59900.00,
      discountPercentage: 0,
      maxParticipants: 10,
      destinations: ['Mirissa', 'Unawatuna'],
      isFeatured: false,
      isActive: true,
    },
  ]

  beforeEach(() => {
    cy.intercept('GET', '**/api/packages?*', {
      statusCode: 200,
      body: { data: mockPackages, totalElements: 2, totalPages: 1 },
    }).as('getPackages')

    cy.visit('/packages')
    cy.wait('@getPackages')
  })

  it('should display packages page', () => {
    cy.contains('Sri Lanka Classic Tour').should('be.visible')
    cy.contains('Beach Paradise Package').should('be.visible')
  })

  it('should show package prices in Rs', () => {
    cy.contains('Rs').should('exist')
  })

  it('should show package duration', () => {
    cy.contains('7').should('exist')
  })

  it('should show destinations', () => {
    cy.contains('Colombo').should('exist')
  })

  it('should navigate to package detail on click', () => {
    cy.contains('Sri Lanka Classic Tour').click()
    cy.url().should('include', '/packages/')
  })

  it('should show error state on failure', () => {
    cy.intercept('GET', '**/api/packages?*', {
      statusCode: 500,
      body: {},
    }).as('getPackagesFail')

    cy.visit('/packages')
    cy.wait('@getPackagesFail')
    cy.contains('Could not load').should('be.visible')
  })
})

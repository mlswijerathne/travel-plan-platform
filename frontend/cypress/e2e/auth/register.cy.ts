describe('Register Page', () => {
  beforeEach(() => {
    cy.visit('/register')
  })

  it('should display role selection step', () => {
    cy.contains('h1', 'Create Account').should('be.visible')
    cy.contains('How would you like to use TravelPlan?').should('be.visible')
  })

  it('should show all four role cards', () => {
    cy.contains('Tourist').should('be.visible')
    cy.contains('Hotel Owner').should('be.visible')
    cy.contains('Tour Guide').should('be.visible')
    cy.contains('Vehicle Owner').should('be.visible')
  })

  it('should have link to login page', () => {
    cy.contains('a', 'Sign In').should('have.attr', 'href', '/login')
  })

  describe('Tourist Registration', () => {
    beforeEach(() => {
      cy.contains('button', 'Tourist').click()
    })

    it('should navigate to tourist form', () => {
      cy.contains('h1', 'Tourist Account').should('be.visible')
      cy.contains('Start planning your Sri Lanka adventure').should('be.visible')
    })

    it('should show back button to return to role selection', () => {
      cy.contains('Back').click()
      cy.contains('h1', 'Create Account').should('be.visible')
    })

    it('should display required fields', () => {
      cy.get('input#firstName').should('be.visible')
      cy.get('input#lastName').should('be.visible')
      cy.get('input#email').should('be.visible')
      cy.get('input#password').should('be.visible')
      cy.get('input#confirmPassword').should('be.visible')
    })

    it('should display optional fields', () => {
      cy.get('input#phoneNumber').should('be.visible')
      cy.contains('Nationality').should('be.visible')
      cy.contains('Budget Level').should('be.visible')
    })

    it('should show error when passwords do not match', () => {
      cy.get('input#firstName').type('John')
      cy.get('input#lastName').type('Doe')
      cy.get('input#email').type('john@test.com')
      cy.get('input#password').type('Test123!')
      cy.get('input#confirmPassword').type('Different!')
      cy.get('button[type="submit"]').click()

      cy.get('[data-sonner-toast]', { timeout: 5000 }).should('contain', 'Passwords do not match')
    })

    it('should show error for short password', () => {
      cy.get('input#firstName').type('John')
      cy.get('input#lastName').type('Doe')
      cy.get('input#email').type('john@test.com')
      cy.get('input#password').type('12345')
      cy.get('input#confirmPassword').type('12345')
      cy.get('button[type="submit"]').click()

      cy.get('[data-sonner-toast]', { timeout: 5000 }).should('contain', 'Password must be at least 6 characters')
    })

    it('should show loading state on submit', () => {
      cy.get('input#firstName').type('John')
      cy.get('input#lastName').type('Doe')
      cy.get('input#email').type('newuser@test.com')
      cy.get('input#password').type('Test123!')
      cy.get('input#confirmPassword').type('Test123!')
      cy.get('button[type="submit"]').click()
      cy.contains('Creating account...').should('be.visible')
    })
  })

  describe('Hotel Owner Registration', () => {
    beforeEach(() => {
      cy.contains('button', 'Hotel Owner').click()
    })

    it('should navigate to hotel owner form', () => {
      cy.contains('h1', 'Hotel Owner Account').should('be.visible')
      cy.contains('Register to list your property').should('be.visible')
    })

    it('should display provider form fields', () => {
      cy.get('input#pFirstName').should('be.visible')
      cy.get('input#pLastName').should('be.visible')
      cy.get('input#pEmail').should('be.visible')
      cy.get('input#pPassword').should('be.visible')
      cy.get('input#pConfirmPassword').should('be.visible')
      cy.get('input#pPhoneNumber').should('be.visible')
    })
  })

  describe('Tour Guide Registration', () => {
    beforeEach(() => {
      cy.contains('button', 'Tour Guide').click()
    })

    it('should navigate to tour guide form', () => {
      cy.contains('h1', 'Tour Guide Account').should('be.visible')
      cy.contains('Register to offer your expertise').should('be.visible')
    })

    it('should display guide-specific fields', () => {
      cy.get('input#gFirstName').should('be.visible')
      cy.get('input#gEmail').should('be.visible')
      cy.get('input#hourlyRate').should('be.visible')
      cy.get('input#dailyRate').should('be.visible')
      cy.get('input#experienceYears').should('be.visible')
      cy.get('textarea#bio').should('be.visible')
      cy.contains('Languages').should('be.visible')
      cy.contains('Specializations').should('be.visible')
    })

    it('should allow selecting language chips', () => {
      cy.contains('button', 'English').click()
      cy.contains('button', 'English').should('have.class', 'bg-primary')
      cy.contains('button', 'Sinhala').click()
      cy.contains('button', 'Sinhala').should('have.class', 'bg-primary')
    })

    it('should require hourly and daily rates', () => {
      cy.get('input#gFirstName').type('Guide')
      cy.get('input#gLastName').type('Test')
      cy.get('input#gEmail').type('guide@test.com')
      cy.get('input#gPassword').type('Test123!')
      cy.get('input#gConfirmPassword').type('Test123!')
      // hourlyRate and dailyRate are required HTML inputs - browser validation prevents submit
      cy.get('input#hourlyRate').then(($el) => {
        expect($el[0].checkValidity()).to.be.false
      })
    })
  })

  describe('Vehicle Owner Registration', () => {
    beforeEach(() => {
      cy.contains('button', 'Vehicle Owner').click()
    })

    it('should navigate to vehicle owner form', () => {
      cy.contains('h1', 'Vehicle Owner Account').should('be.visible')
      cy.contains('Register to rent your vehicles').should('be.visible')
    })
  })

  describe('Role Query Param', () => {
    it('should auto-select tourist role from query param', () => {
      cy.visit('/register?role=TOURIST')
      cy.contains('h1', 'Tourist Account').should('be.visible')
    })

    it('should auto-select hotel owner role from query param', () => {
      cy.visit('/register?role=HOTEL_OWNER')
      cy.contains('h1', 'Hotel Owner Account').should('be.visible')
    })

    it('should auto-select tour guide role from query param', () => {
      cy.visit('/register?role=TOUR_GUIDE')
      cy.contains('h1', 'Tour Guide Account').should('be.visible')
    })

    it('should auto-select vehicle owner role from query param', () => {
      cy.visit('/register?role=VEHICLE_OWNER')
      cy.contains('h1', 'Vehicle Owner Account').should('be.visible')
    })
  })
})

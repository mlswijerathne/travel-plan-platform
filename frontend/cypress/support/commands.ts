/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Login via Supabase UI. Skips the test if credentials are not configured.
       */
      login(role?: 'tourist' | 'hotel_owner' | 'tour_guide' | 'vehicle_owner' | 'admin'): Chainable<void>

      /**
       * Login through the UI (real login flow)
       */
      loginViaUI(email: string, password: string): Chainable<void>

      /**
       * Intercept API calls and return mock data
       */
      interceptApi(method: string, path: string, response: object, statusCode?: number): Chainable<void>

      /**
       * Wait for page to be fully loaded (no loading skeletons)
       */
      waitForPageLoad(): Chainable<void>

      /**
       * Get element by data-testid
       */
      getByTestId(testId: string): Chainable<JQuery<HTMLElement>>
    }
  }
}

// ── Login via UI ──────────────────────────────────────────────────────
Cypress.Commands.add('loginViaUI', (email: string, password: string) => {
  cy.visit('/login')
  cy.get('input#email').type(email)
  cy.get('input#password').type(password)
  cy.get('button[type="submit"]').click()
  cy.url().should('not.include', '/login', { timeout: 15000 })
})

// ── Login by role ────────────────────────────────────────────────────
Cypress.Commands.add('login', (role: string = 'tourist') => {
  const roleMap: Record<string, { emailEnv: string; passwordEnv: string; appRole: string; redirect: string }> = {
    tourist: {
      emailEnv: 'TOURIST_EMAIL',
      passwordEnv: 'TOURIST_PASSWORD',
      appRole: 'TOURIST',
      redirect: '/profile',
    },
    hotel_owner: {
      emailEnv: 'HOTEL_OWNER_EMAIL',
      passwordEnv: 'HOTEL_OWNER_PASSWORD',
      appRole: 'HOTEL_OWNER',
      redirect: '/provider/dashboard',
    },
    tour_guide: {
      emailEnv: 'TOUR_GUIDE_EMAIL',
      passwordEnv: 'TOUR_GUIDE_PASSWORD',
      appRole: 'TOUR_GUIDE',
      redirect: '/provider/dashboard',
    },
    vehicle_owner: {
      emailEnv: 'VEHICLE_OWNER_EMAIL',
      passwordEnv: 'VEHICLE_OWNER_PASSWORD',
      appRole: 'VEHICLE_OWNER',
      redirect: '/provider/dashboard',
    },
    admin: {
      emailEnv: 'ADMIN_EMAIL',
      passwordEnv: 'ADMIN_PASSWORD',
      appRole: 'ADMIN',
      redirect: '/admin',
    },
  }

  const config = roleMap[role]
  if (!config) throw new Error(`Unknown role: ${role}`)

  const email = Cypress.env(config.emailEnv)
  const password = Cypress.env(config.passwordEnv)

  if (!email || !password) {
    cy.log(`⚠️ SKIPPING: No credentials for role "${role}". Set ${config.emailEnv} and ${config.passwordEnv} in cypress.env.json`)
    // Use Cypress skip pattern
    const test = (cy as any).state('runnable')
    if (test) test.skip()
    return
  }

  cy.loginViaUI(email, password)
})

// ── Intercept API ────────────────────────────────────────────────────
Cypress.Commands.add('interceptApi', (method: string, path: string, response: object, statusCode: number = 200) => {
  cy.intercept(method, `${Cypress.env('API_BASE_URL')}${path}*`, {
    statusCode,
    body: response,
  })
})

// ── Wait for page load ───────────────────────────────────────────────
Cypress.Commands.add('waitForPageLoad', () => {
  cy.get('.animate-pulse', { timeout: 15000 }).should('not.exist')
})

// ── Get by test ID ───────────────────────────────────────────────────
Cypress.Commands.add('getByTestId', (testId: string) => {
  return cy.get(`[data-testid="${testId}"]`)
})

export {}

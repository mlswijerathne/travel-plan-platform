import { defineConfig } from 'cypress'

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:3000',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10000,
    requestTimeout: 15000,
    responseTimeout: 15000,
    video: false,
    screenshotOnRunFailure: true,
    env: {
      API_BASE_URL: 'http://localhost:8060',
      // Test accounts - set these in cypress.env.json
      TOURIST_EMAIL: 'tourist@test.com',
      TOURIST_PASSWORD: 'Test123!',
      HOTEL_OWNER_EMAIL: 'hotelowner@test.com',
      HOTEL_OWNER_PASSWORD: 'Test123!',
      TOUR_GUIDE_EMAIL: 'tourguide@test.com',
      TOUR_GUIDE_PASSWORD: 'Test123!',
      VEHICLE_OWNER_EMAIL: 'vehicleowner@test.com',
      VEHICLE_OWNER_PASSWORD: 'Test123!',
      ADMIN_EMAIL: 'admin@test.com',
      ADMIN_PASSWORD: 'Test123!',
    },
  },
})

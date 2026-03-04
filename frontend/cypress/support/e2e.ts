import './commands'

// Hide fetch/XHR requests from command log for cleaner output
const app = window.top
if (app && !app.document.head.querySelector('[data-hide-command-log-request]')) {
  const style = app.document.createElement('style')
  style.innerHTML = '.command-name-request, .command-name-xhr { display: none }'
  style.setAttribute('data-hide-command-log-request', '')
  app.document.head.appendChild(style)
}

// Prevent uncaught exceptions from failing tests
Cypress.on('uncaught:exception', (err) => {
  // Next.js hydration errors
  if (err.message.includes('Hydration') || err.message.includes('hydrat')) {
    return false
  }
  // React Query errors
  if (err.message.includes('QueryClient')) {
    return false
  }
  // Supabase auth errors in test env
  if (err.message.includes('AuthSession') || err.message.includes('refresh_token')) {
    return false
  }
  return true
})

/**
 * Proxy configuration for dev environment
 * Forward /api/* requests to Spring Boot backend
 */
export default {
  dev: {
    '/api/': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
  test: {
    '/api/': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
  pre: {
    '/api/': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
};

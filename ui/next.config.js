/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  async rewrites() {
    return [
      {
        source: '/api/upload',
        destination: 'http://backend:8080/upload',        // <— “backend” not “localhost”
      },
      {
        source: '/api/download/:port',
        destination: 'http://backend:8080/download/:port', // <— ditto
      },
    ];
  },
}

module.exports = nextConfig

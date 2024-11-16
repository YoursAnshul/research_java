/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,js}"],
  theme: {
    colors: {
      'white': '#ffffff',
      'black': '#000000',
      'duke-blue': {
        'dark': '#001a57',
        DEFAULT: '#00569b',
        'light': '#7ba6dc',
      },
    },
    extend: {},
  },
  plugins: [],
}


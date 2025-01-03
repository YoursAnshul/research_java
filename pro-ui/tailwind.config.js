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
      'button-blue': {
        DEFAULT: '#0680CD',
        'light': '#0680cd47',
      },
      'duke-gray': {
        'dark': '#777777',
        DEFAULT: '#b5b5b5',
        'light': '#e0e0e0',
      }
    },
    extend: {},
  },
  plugins: [],
}


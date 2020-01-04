/*
Common configuration for axios.
*/

import axios from 'axios';

export default axios
  .create({
    baseURL: process.env.VUE_APP_BACKEND_HOST_URL,
    headers: {
      'Access-Control-Allow-Origin': process.env.VUE_APP_FRONTEND_HOST_URL,
    },
  });

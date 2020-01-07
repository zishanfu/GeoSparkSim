<template>
  <div id="app">
    <Map/>
    <div id="panel">
      <img
        src="./assets/GeoSparkSim.png"
        alt="GeoSparkSim Logo"
        class="center"
      >
      <form
        class="form-group"
        novalidate
        @submit.prevent="submit"
      >
        <div class="form-group row">
          <label
            for="vehicles"
            class="col-sm-6 col-form-label"
          >Number of Vehicles:</label>
          <div class="col-sm-4">
            <input
              type="text"
              v-model="vehicles"
              class="form-control"
              id="vehicles"
            >
          </div>
        </div>
        <div class="form-group row">
          <label
            for="steps"
            class="col-sm-6 col-form-label"
          >Simulation Steps:</label>
          <div class="col-sm-4">
            <input
              type="text"
              v-model="steps"
              class="form-control"
              id="steps"
            >
          </div>
        </div>
        <div class="form-group row">
          <label
            for="tps"
            class="col-sm-6 col-form-label"
          >Time per Step:</label>
          <div class="col-sm-4">
            <input
              type="text"
              v-model="tps"
              class="form-control"
              id="tps"
            >
          </div>
        </div>
        <div class="form-group">
          <label for="vg">Vehicle Generation:</label>
          <select
            class="form-control form-control-lg"
            v-model="vehicleGeneration"
          >
            <option value="DSO">
              Data-space oriented approach(DSO)
            </option>
            <option value="NB">
              Network-based approach(NB)
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="output">Output Path:</label>
          <input
            type="text"
            v-model="output"
            class="form-control"
            id="output"
          >
        </div>
        <input
          class="btn btn-primary submit_button"
          type="submit"
          id="run-simulation"
          placeholder="Run Simulation"
        >
        <input
          class="btn btn-primary submit_button"
          type="reset"
          id="show-vis"
          placeholder="Show Visualization"
        >
        <div class="form-group">
          <pre
            class="form-control"
            id="updates"
            rows="5"
          >{{ updates }}</pre>
        </div>
      </form>
    </div>
    <Modal
      v-if="hasError"
      :show-modal.sync="hasError"
      :modal="modal"
    />
  </div>
</template>

<script>
import Map from './components/Map';
import Modal from './components/Modal';
import axios from './utils/http-common';
import $ from "jquery";

const URI = 'simulation';

export default {
  name: 'App',
  components: {
    Map,
    Modal
  },
  data() {
    return {
      vehicles: 4000,
      steps: 600,
      tps: 1,
      output: 'hdfs',
      vehicleGeneration: 'DSO',
      hasError: false,
      updates: 'Updating GeoSparkSim status...'
    };
  },
  methods: {
    submit() {
      const bounds = JSON.parse($('#bounds').text());
      const topleft_lat = bounds.topleft.lat,
            topleft_lng = bounds.topleft.lng,
            bottomright_lat = bounds.bottomright.lat,
            bottomright_lng = bounds.bottomright.lng;

      const simConfig = {
        'lat1': topleft_lat,
        'lon1': topleft_lng,
        'lat2': bottomright_lat,
        'lon2': bottomright_lng,
        'total': this.vehicles,
        'outputPath': this.output,
        'step': this.steps,
        'timestep': this.tps,
        'type': this.vehicleGeneration
      };

      console.log(simConfig);

      return axios
        .post(URI, JSON.stringify(simConfig), { headers: { 'content-type': 'application/json' } })
        .catch(error => this.errorHandler(error));
    },
    errorHandler(error) {
      if (error.response) {
        const { data } = error.response;
        this.hasError = true;
        this.modal.title = 'yoyo';
        this.modal.content = `${data.error}\n${data.timestamp}`;
      }
    },
  }
}
</script>

<style scoped>
@import url('components/Main.css');
</style>

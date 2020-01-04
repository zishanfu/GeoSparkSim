<template>
  <div id="map">
    <div id="bounds" style="display: none;"></div>
    <l-map
      :zoom="zoom"
      :center="center"
      ref="map"
      style="height: 620px; width: 70%; float:left"
    >
      <l-tile-layer
        :url="url"
        :attribution="attribution"
      />
      <v-geosearch :options="geosearchOptions" />
    </l-map>
  </div>
</template>

<script>
import { LMap,LTileLayer } from "vue2-leaflet";
import { OpenStreetMapProvider } from 'leaflet-geosearch';
import VGeosearch from 'vue2-leaflet-geosearch';
import 'leaflet-draw';
import $ from "jquery";

  export default {
    name: 'Map',
    components: {
      LMap,
      LTileLayer,
      VGeosearch
    },
    data() {
      return {
        zoom: 15,
        center: [33.422123, -111.927992],
        url: 'http://{s}.tile.osm.org/{z}/{x}/{y}.png',
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
        geosearchOptions: {
          provider: new OpenStreetMapProvider(),
        },
      }
    },
    mounted(){
      this.$nextTick(() => {
        var mapObj = this.$refs.map.mapObject;
        const drawnItems = new window.L.FeatureGroup().addTo(mapObj);
        const drawControlFull = new window.L.Control.Draw({
            edit: {
              featureGroup: drawnItems,
              remove: true,
              edit: true
            },
            position: 'topright',
            draw: {
              polyline: false,
              polygon: false,
              circle: false,
              marker: false,
              circlemarker: false
            }
        });
        const drawControlEditOnly = new window.L.Control.Draw({
            edit: {
              featureGroup: drawnItems,
              remove: true,
              edit: true
            },
            position: 'topright',
            draw: false,
        });

        mapObj.addControl(drawControlFull);

        const data = mapObj.on(window.L.Draw.Event.CREATED, function (e) {
          const layer = e.layer;
          drawnItems.addLayer(layer);
          drawControlFull.remove(mapObj);
          drawControlEditOnly.addTo(mapObj)

          const bounds = layer.getLatLngs();
          const data = {
            topleft: bounds[0][1],
            bottomright: bounds[0][3]
          }
          $('#bounds').text(JSON.stringify(data));
          drawnItems.addLayer(layer);
        });

        mapObj.on(window.L.Draw.Event.DELETED, function(e) {
          if (drawnItems.getLayers().length === 0){
            drawControlEditOnly.remove(mapObj);
            drawControlFull.addTo(mapObj);
          }
        });
      })
    }
  }
</script>

<style scoped>
@import "~leaflet-draw/dist/leaflet.draw.css";
@import "~leaflet/dist/leaflet.css";
</style>

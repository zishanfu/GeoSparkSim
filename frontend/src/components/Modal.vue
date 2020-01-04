<!--
A reusable modal to use for any popups that might need to be displayed
The slots can be used to put any content necessary in a given section

props:
  - showModal: a synced Boolean value that the Modal will set to false to tell its parent to
               stop displaying it
  - modal: an object representing the content in modal HTML.
    - modal.title: a string of title describing where is the error, like option, images, etc...
    - modal.content: a string of message describing what is the error

event listeners:
  - accept: a function called in the parent when submit is clicked
-->

<template>
  <transition name="modal-fade">
    <div class="modal-backdrop-geosparksim">
      <div
        class="modal-geosparksim"
        role="dialog"
        aria-labelledby="modalTitle"
        aria-describedby="modalDescription"
      >
        <header class="modal-header">
          <button
            type="button"
            class="btn-close"
            @click="close"
            aria-label="Close modal"
          >
            x
          </button>
          <pre name="header"> {{ modal.title }}</pre>
        </header>
        <section class="modal-body">
          <pre name="body"> {{ modal.content }} </pre>
        </section>
        <section class="modal-footer">
          <slot name="footer">
            <button
              type="button"
              class="btn-accept"
              @click="accept"
              aria-label="Accept"
            >
              Accept
            </button>
          </slot>
        </section>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'Modal',
  props: {
    showModal: {
      type: Boolean,
      default: false,
    },
    modal: {
      title: String,
      content: String,
    },
  },

  methods: {
    /**
     * A function called when the close button is clicked to update the showModal
     * prop to false and tell the parent to stop displaying the modal
     */
    close() {
      this.$emit('update:showModal', false);
    },

    /**
     * Called when the accept button is clicked, the function calls the
     * custom event attached by the parent and closes the modal
     */
    accept() {
      this.$emit('accept');
      this.close();
    },
  },
};
</script>

<style scoped>
@import url('Modal.css');
</style>

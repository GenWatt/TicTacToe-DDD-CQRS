<template>
    <button 
        :class="computedClasses" 
        :disabled="disabled || loading" 
        @click="$emit('click', $event)"
    >
        <span v-if="loading" class="loader"></span>
        <slot v-else>{{ text }}</slot>
    </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
    text?: string;
    type?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info';
    size?: 'sm' | 'md' | 'lg';
    fullWidth?: boolean;
    disabled?: boolean;
    loading?: boolean;
    customClass?: string;
}

const props = withDefaults(defineProps<Props>(), {
    text: '',
    type: 'primary',
    size: 'md',
    fullWidth: false,
    disabled: false,
    loading: false,
    customClass: ''
});

defineEmits(['click']);

const computedClasses = computed(() => {
    return [
        'base-button',
        `btn-${props.type}`,
        `size-${props.size}`,
        { 'full-width': props.fullWidth },
        { 'disabled': props.disabled },
        { 'loading': props.loading },
        props.customClass
    ];
});
</script>

<style scoped>
.base-button {
    border: none;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s, opacity 0.2s;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

/* Button types */
.btn-primary {
    background-color: #4CAF50;
    color: white;
}
.btn-primary:hover:not(.disabled) {
    background-color: #45a049;
}

.btn-secondary {
    background-color: #f5f5f5;
    color: #333;
    border: 1px solid #ddd;
}
.btn-secondary:hover:not(.disabled) {
    background-color: #e9e9e9;
}

.btn-success {
    background-color: #28a745;
    color: white;
}
.btn-success:hover:not(.disabled) {
    background-color: #218838;
}

.btn-danger {
    background-color: #dc3545;
    color: white;
}
.btn-danger:hover:not(.disabled) {
    background-color: #c82333;
}

.btn-warning {
    background-color: #ffc107;
    color: #212529;
}
.btn-warning:hover:not(.disabled) {
    background-color: #e0a800;
}

.btn-info {
    background-color: #17a2b8;
    color: white;
}
.btn-info:hover:not(.disabled) {
    background-color: #138496;
}

/* Button sizes */
.size-sm {
    padding: 6px 12px;
    font-size: 14px;
}

.size-md {
    padding: 10px 15px;
    font-size: 16px;
}

.size-lg {
    padding: 12px 20px;
    font-size: 18px;
}

/* Other styles */
.full-width {
    width: 100%;
}

.disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.loading {
    position: relative;
    color: transparent;
}

.loader {
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    border-top-color: #fff;
    animation: spin 1s linear infinite;
    position: absolute;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}
</style>
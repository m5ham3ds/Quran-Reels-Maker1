/**
 * config.js
 * Configuration settings for the Quran Ayah Generator application
 */

const CONFIG = {
    // API configuration
    api: {
        // Base URL for API requests (set to /preprod for the hosted environment)
        baseUrl: '',
        
        // Data directory path
        dataPath: 'data',
        
        // Data endpoint for fetching CSV files
        dataEndpoint: 'get_data.php',
        
        // Save CSV endpoint
        saveCsvEndpoint: 'save-csv.php',
        
        // Function to get the full API URL
        getUrl: function(endpoint) {
            return this.baseUrl ? `${this.baseUrl}/${endpoint}`.replace(/\/+/g, '/') : endpoint;
        },
        
        // Function to get the data file URL
        getDataUrl: function(filename) {
            // For direct file access, use the data path
            // For API access, use the data endpoint with a file parameter
            if (this.baseUrl) {
                // We're in a hosted environment, use the data endpoint
                return this.getUrl(`${this.dataEndpoint}?file=${filename}`);
            } else {
                // We're in a local environment, use direct file access
                return this.getUrl(`${this.dataPath}/${filename}`);
            }
        },
        
        // Function to get the save CSV URL
        getSaveCsvUrl: function() {
            return this.getUrl(this.saveCsvEndpoint);
        }
    },
    
    // Debug mode - set to true to enable console logging
    debug: true,
    
    // Log function that only logs in debug mode
    log: function(message, data) {
        if (this.debug) {
            if (data) {
                console.log(`[Quran App] ${message}`, data);
            } else {
                console.log(`[Quran App] ${message}`);
            }
        }
    }
};

// Make the configuration available globally
window.CONFIG = CONFIG;

// Helper functions for easier access
function getApiUrl(endpoint) {
    return CONFIG.api.getUrl(endpoint);
}

function getDataUrl(filename) {
    return CONFIG.api.getDataUrl(filename);
}

function getSaveCsvUrl() {
    return CONFIG.api.getSaveCsvUrl();
}

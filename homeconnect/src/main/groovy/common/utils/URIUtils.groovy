/**
 * Simple utilities for URI manipulation
 */
def URIUtils_create() {
    def instance = [:];
    
    instance.toQueryString = { Map m ->
    	return m.collect { k, v -> "${k}=${new URI(null, null, v.toString(), null)}" }.sort().join("&")
    }

    return instance;
}
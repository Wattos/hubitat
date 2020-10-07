import groovy.transform.Field
import groovy.json.JsonSlurper

/** Includes-ON **/
/* include ../common/api/HomeConnectAPI.groovy */
/* include ../common/utils/URIUtils.groovy */
/** Includes-OFF **/

definition(
    name: 'Home Connect Integration',
    namespace: 'wattos',
    author: 'Filip Wieladek',
    description: 'Integrates with Home Connect',
    category: 'MyApps',
    iconUrl: '',
    iconX2Url: '',
    iconX3Url: ''
)

@Field HomeConnectAPI = HomeConnectAPI_create(oAuthTokenFactory: {return getOAuthAuthToken()});
@Field URIUtils = URIUtils_create();

//  ===== Settings =====
private getClientId() { settings.clientId }
private getClientSecret() { settings.clientSecret }


//	===== Pages =====

preferences {
	page(name: "pageIntro")
    page(name: "pageAuthentication")
    page(name: "pageDevices")
}

def pageIntro() {
    log.debug("Showing Introduction Page");

    return dynamicPage(
		name: 'pageIntro',
		title: 'Home Connect Introduction', 
		nextPage: 'pageAuthentication',
		install: false, 
		uninstall: true) {
		    section("""\
                    |This application connects to the Home Connect service.
                    |It will allow you to monitor your smart appliances from Home Connect within Hubitat.
                    |
                    |Please note, before you can proceed, you will need to:
                    | 1. Sign up at <a href="https://developer.home-connect.com/">Home Connect Developer Portal</a>.
                    | 2. Go to <a href="https://developer.home-connect.com/applications">Home Connect Applications</a>.
                    | 3. Register a new application with the following values:
                    |    * <b>Application ID</b>: hubitat-homeconnect-integration
                    |    * <b>OAuth Flow</b>: Authorization Code Grant Flow
                    |    * <b>Redirect URI</b>: ${getFullApiServerUrl()}/oauth/callback
                    |    * You can leave the rest as blank
                    | 4. Copy the following values down below.
                    |""".stripMargin()) {}
            section('Enter your Home Connect Developer Details.') {
                input name: 'clientId', title: 'Client ID', type: 'text', required: true
                input name: 'clientSecret', title: 'Client Secret', type: 'text', required: true
            }
		    section('''\
                    |Press 'Next' to connect to your Home Connect Account.
                    |'''.stripMargin()) {}
	}
}

def pageAuthentication() {
    log.debug("Showing Authentication Page");

    if (!state.accessToken) {
		state.accessToken = createAccessToken();
	}

    return dynamicPage(
        name: 'pageAuthentication', 
        title: 'Home Connect Authentication',
        nextPage: 'pageDevices',
        install: false, 
        uninstall: false) {
        section() {
            def title = "Connect to Home Connect"
            if (state.oAuthAuthToken) {
                title = "Re-connect to Home Connect"
                paragraph '<b>Success!</b> You are connected to Home Connect. Please press the Next button.'
            } else {
                paragraph 'To continue, you need to connect your hubitat to Home connect. Please press the button below to connect'
            }
            
            href url: generateOAuthUrl(), style:'external', required:false, 'title': title
        }
    }
}

def pageDevices() {
    return dynamicPage(
        name: 'pageDevices', 
        title: 'Home Connect Devices',
        install: true, 
        uninstall: true) {
        section() {
            paragraph 'Select the following devices';
            HomeConnectAPI.getHomeAppliances() { devices ->
                input name: 'devices', title: 'Select Devices', type: 'enum', required: true, multiple:true, options: devices.collectEntries({[it.haId, "${it.name} (${it.type}) (${it.haId})"]})
            }
        }
    }

}


// ===== Authentication =====
// See Home Connect Developer documentation here: https://developer.home-connect.com/docs/authorization/flow
private final OAUTH_AUTHORIZATION_URL() { 'https://api.home-connect.com/security/oauth/authorize' }
private final OAUTH_TOKEN_URL() { 'https://api.home-connect.com/security/oauth/token' }

mappings {
	path("/oauth/callback") {action: [GET: "oAuthCallback"]};
}

def generateOAuthUrl() {
    state.oAuthInitState = UUID.randomUUID().toString();
    def params = [
        'client_id': getClientId(),
        'redirect_uri': getOAuthRedirectUrl(),
        'response_type': 'code',
        'scope': 'IdentifyAppliance Monitor',
        'state': state.oAuthInitState
    ];
    return "${OAUTH_AUTHORIZATION_URL()}?${URIUtils.toQueryString(params)}";
}

def getOAuthRedirectUrl() {
    return "${getFullApiServerUrl()}/oauth/callback?access_token=${state.accessToken}";
}

def oAuthCallback() {
    log.debug("Received oAuth callback");

    def code = params.code;
	def oAuthState = params.state;
    if (oAuthState != state.oAuthInitState) {
        log.error "Init state did not match our state on the callback. Ignoring the request"
        return renderOAuthFailure();
    }
    
    // Prevent any replay attacks and re-initialize the state
    state.oAuthInitState = null;
    state.oAuthRefreshToken = null;
    state.oAuthAuthToken = null;
    state.oAuthTokenExpires = null;

    acquireOAuthToken(code);

    if (!state.oAuthAuthToken) {
        return renderOAuthFailure();
    }
    renderOAuthSuccess();
}

def acquireOAuthToken(String code) {
    log.debug("Acquiring OAuth Authentication Token");
    apiRequestAccessToken([
        'grant_type': 'authorization_code',
        'code': code,
        'client_id': getClientId(),
        'client_secret': getClientSecret(),
        'redirect_uri': getOAuthRedirectUrl(),
    ]);
}

def refreshOAuthToken() {
    log.debug("Refreshing OAuth Authentication Token");
    apiRequestAccessToken([
        'grant_type': 'refresh_token',
        'refresh_token': state.oAuthRefreshToken,
        'client_secret': getClientSecret(),
    ]);
}

def apiRequestAccessToken(body) {
    try {
        httpPost(uri: OAUTH_TOKEN_URL(), requestContentType: 'application/x-www-form-urlencoded', body: body) { resp ->
            if (resp && resp.data && resp.success) {
                state.oAuthRefreshToken = resp.data.refresh_token
                state.oAuthAuthToken = resp.data.access_token
                state.oAuthTokenExpires = now() + (resp.data.expires_in * 1000)
            } else {
                log.error "Failed to acquire OAuth Authentication token. Response was not successful."
            }
        }
    } catch (e) {
        log.error "Failed to acquire OAuth Authentication token due to Exception: ${e}"
    }
}

def getOAuthAuthToken() {
    // Expire the token 1 minute before to avoid race conditions
    if(now() >= state.oAuthTokenExpires - 60_000) {
		refreshOAuthToken();
	}
    return state.oAuthAuthToken;
}

def renderOAuthSuccess() {
	render contentType: 'text/html', data: '''
	<p>Your Home Connect Account is now connected to Hubitat</p>
	<p>Close this window to continue setup.</p>
	'''
}

def renderOAuthFailure() {
	render contentType: 'text/html', data: '''
		<p>Unable to connect to Home Connect. You can see the logs for more information</p>
		<p>Close this window to try again.</p>
	'''
}

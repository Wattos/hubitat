import groovy.transform.Field
import groovy.json.JsonSlurper

/**
 * The Home Connect API
 *
 * The complete documentation can be found here: https://apiclient.home-connect.com/#/
 */
def HomeConnectAPI_create(Map params = [:]) {
    def defaultParams = [
        apiUrl: 'https://api.home-connect.com',
        oAuthTokenFactory: null
    ]

    def resolvedParams = defaultParams << params;
    def apiUrl = resolvedParams['apiUrl']
    def oAuthTokenFactory = resolvedParams['oAuthTokenFactory']

    def instance = [:];
    def json = new JsonSlurper();

    def authHeaders = {
        return ['Authorization': "Bearer ${oAuthTokenFactory()}"]
    }

    def apiGet = { path, closure ->
        log.debug("API Get Request to Home Connect with path $path and query $query")
        return httpGet(uri: apiUrl,
                'path': path,
                'headers': authHeaders()) { resp -> 
            closure.call(json.parseText(resp.data.text));
        }
    };

    /**
     * Get all home appliances which are paired with the logged-in user account.
     *
     * This endpoint returns a list of all home appliances which are paired
     * with the logged-in user account. All paired home appliances are returned
     * independent of their current connection state. The connection state can
     * be retrieved within the field 'connected' of the respective home appliance.
     * The haId is the primary access key for further API access to a specific
     * home appliance.
     *
     * Example return value:
     * [
     *    {
     *      "name": "My Bosch Oven",
     *      "brand": "BOSCH",
     *      "vib": "HNG6764B6",
     *      "connected": true,
     *      "type": "Oven",
     *      "enumber": "HNG6764B6/09",
     *      "haId": "BOSCH-HNG6764B6-0000000011FF"
     *    }
     * ]
     */
    instance.getHomeAppliances = { closure ->
        log.info('Retrieving Home Appliances from Home Connect')
        apiGet('/api/homeappliances') { resp ->
            closure.call(resp.data.homeappliances)
        }
    };

    /**
     * Get a specfic home appliances which are paired with the logged-in user account.
     *
     * This endpoint returns a specific home appliance which is paired with the
     * logged-in user account. It is returned independent of their current
     * connection state. The connection state can be retrieved within the field
     * 'connected' of the respective home appliance.
     * The haId is the primary access key for further API access to a specific
     * home appliance.
     *
     * Example return value:
     *
     * {
     *   "name": "My Bosch Oven",
     *   "brand": "BOSCH",
     *   "vib": "HNG6764B6",
     *   "connected": true,
     *   "type": "Oven",
     *   "enumber": "HNG6764B6/09",
     *   "haId": "BOSCH-HNG6764B6-0000000011FF"
     * }
     */
    instance.getHomeAppliance = { haId, closure ->
        log.info("Retrieving Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliance/${haId}") { resp ->
            closure.call(resp.data)
        }
    };

    /**
     * Get all programs of a given home appliance.
     *
     * Example return value:
     *
     * [
     *   {
     *     "key": "Cooking.Oven.Program.HeatingMode.HotAir",
     *     "constraints": {
     *       "available": true,
     *       "execution": "selectandstart"
     *     }
     *   },
     *   {
     *     "key": "Cooking.Oven.Program.HeatingMode.TopBottomHeating",
     *     "constraints": {
     *       "available": true,
     *       "execution": "selectandstart"
     *     }
     *   },
     *   {
     *     "key": "Cooking.Oven.Program.HeatingMode.PizzaSetting",
     *     "constraints": {
     *       "available": true,
     *       "execution": "selectonly"
     *     }
     *   }
     * ]
     */
    instance.getPrograms = { haId, closure ->
        log.info("Retrieving All Programs of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs") { resp ->
            closure.call(resp.data.programs)
        }
    };

    /**
     * Get all programs which are currently available on the given home appliance.
     *
     * Example return value:
     *
     * [
     *   {
     *     "key": "Cooking.Oven.Program.HeatingMode.HotAir",
     *     "constraints": {
     *       "available": true,
     *       "execution": "selectandstart"
     *     }
     *   },
     *   {
     *     "key": "Cooking.Oven.Program.HeatingMode.TopBottomHeating",
     *     "constraints": {
     *       "available": true,
     *       "execution": "selectandstart"
     *     }
     *   },
     *   {
     *     "key": "Cooking.Oven.Program.HeatingMode.PizzaSetting",
     *     "constraints": {
     *       "available": true,
     *       "execution": "selectonly"
     *     }
     *   }
     * ]
     */
    instance.getAvailablePrograms = { haId, closure ->
        log.info("Retrieving All Programs of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/available") { resp ->
            closure.call(resp.data.programs)
        }
    };

    /**
     * Get specific available program.
     *
     * Example return value:
     *
     * {
     *     "key": "Cooking.Oven.Program.HeatingMode.HotAir",
     *     "options": [
     *       {
     *         "key": "Cooking.Oven.Option.SetpointTemperature",
     *         "type": "Int",
     *         "unit": "°C",
     *         "constraints": {
     *           "min": 30,
     *           "max": 250
     *         }
     *       },
     *       {
     *         "key": "BSH.Common.Option.Duration",
     *         "type": "Int",
     *         "unit": "seconds",
     *         "constraints": {
     *           "min": 1,
     *           "max": 86340
     *         }
     *       }
     *     ]
     * }
     */
    instance.getAvailableProgram = { haId, programKey, closure ->
        log.info("Retrieving the '${programKey}' Program of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/available/${programKey}") { resp ->
            closure.call(resp.data)
        }
    };

    /**
     * Get program which is currently executed.
     *
     * Example return value:
     *
     * {
     *     "key": "Cooking.Oven.Program.HeatingMode.HotAir",
     *     "options": [
     *       {
     *         "key": "Cooking.Oven.Option.SetpointTemperature",
     *         "value": 230,
     *         "unit": "°C"
     *       },
     *       {
     *         "key": "BSH.Common.Option.Duration",
     *         "value": 1200,
     *         "unit": "seconds"
     *       }
     *     ]
     * }
     */
    instance.getActiveProgram = { haId, closure ->
        log.info("Retrieving the active Program of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/active") { resp ->
            closure.call(resp.data)
        }
    };

    /**
     * Get all options of the active program like temperature or duration.
     *
     * Example return value:
     *
     * {
     *     "key": "Cooking.Oven.Program.HeatingMode.HotAir",
     *     "options": [
     *       {
     *         "key": "Cooking.Oven.Option.SetpointTemperature",
     *         "value": 230,
     *         "unit": "°C"
     *       },
     *       {
     *         "key": "BSH.Common.Option.Duration",
     *         "value": 1200,
     *         "unit": "seconds"
     *       }
     *     ]
     *   }
     */
    instance.getActiveProgramOptions = { haId, closure ->
        log.info("Retrieving the active Program Options of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/active/options") { resp ->
            closure.call(resp.data.options)
        }
    };

    /**
     * Get one specific option of the active program, e.g. the duration.
     *
     * Example return value:
     *
     * {
     *  "key": "Cooking.Oven.Option.SetpointTemperature",
     *  "value": 180,
     *  "unit": "°C"
     * }
     */
    instance.getActiveProgramOption = { haId, optionKey, closure ->
        log.info("Retrieving the active Program Option '${optionKey}' of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/active/options/${optionKey}") { resp ->
            closure.call(resp.data.options)
        }
    };

    /**
     * Get the program which is currently selected.
     *
     * In most cases the selected program is the program which is currently shown on the display of the home appliance.
     * This program can then be manually adjusted or started on the home appliance itself. 
     * 
     * Example return value:
     *
     * {
     *     "key": "Cooking.Oven.Program.HeatingMode.HotAir",
     *     "options": [
     *       {
     *         "key": "Cooking.Oven.Option.SetpointTemperature",
     *         "value": 230,
     *         "unit": "°C"
     *       },
     *       {
     *         "key": "BSH.Common.Option.Duration",
     *         "value": 1200,
     *         "unit": "seconds"
     *       }
     *     ]
     * }
     */
    instance.getSelectedProgram = { haId, closure ->
        log.info("Retrieving the selected Program of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/selected") { resp ->
            closure.call(resp.data)
        }
    };

    /**
     * Get all options of selected program.
     *
     * Example return value:
     *
     * {
     *     "key": "Cooking.Oven.Program.HeatingMode.HotAir",
     *     "options": [
     *       {
     *         "key": "Cooking.Oven.Option.SetpointTemperature",
     *         "value": 230,
     *         "unit": "°C"
     *       },
     *       {
     *         "key": "BSH.Common.Option.Duration",
     *         "value": 1200,
     *         "unit": "seconds"
     *       }
     *     ]
     *   }
     */
    instance.getSelectedProgramOptions = { haId, closure ->
        log.info("Retrieving the selected Program Options of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/selected/options") { resp ->
            closure.call(resp.data.options)
        }
    };

    /**
     * Get specific option of selected program
     *
     * Example return value:
     *
     * {
     *  "key": "Cooking.Oven.Option.SetpointTemperature",
     *  "value": 180,
     *  "unit": "°C"
     * }
     */
    instance.getSelectedProgramOption = { haId, optionKey, closure ->
        log.info("Retrieving the selected Program Option ${optionKey} of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/programs/selected/options/${optionKey}") { resp ->
            closure.call(resp.data.options)
        }
    };

        
    /**
     * Get current status of home appliance
     *
     * A detailed description of the available status can be found here:
     *
     * https://developer.home-connect.com/docs/api/status/remotecontrolactivationstate - Remote control activation state
     * https://developer.home-connect.com/docs/api/status/remotestartallowancestate - Remote start allowance state
     * https://developer.home-connect.com/docs/api/status/localcontrolstate - Local control state
     * https://developer.home-connect.com/docs/status/operation_state - Operation state
     * https://developer.home-connect.com/docs/status/door_state - Door state
     *
     * Several more device-specific states can be found at https://developer.home-connect.com/docs/api/status/remotecontrolactivationstate.
     *
     * Example return value:
     *
     * [
     *  {
     *    "key": "BSH.Common.Status.OperationState",
     *    "value": "BSH.Common.EnumType.OperationState.Ready"
     *  },
     *  {
     *    "key": "BSH.Common.Status.LocalControlActive",
     *    "value": true
     *  }
     * ]
     */
    instance.getStatus = { haId, closure ->
        log.info("Retrieving the status of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/status") { resp ->
            closure.call(resp.data.status)
        }
    };

    /**
     * Get current status of home appliance
     *
     * A detailed description of the available status can be found here:
     *
     * https://developer.home-connect.com/docs/api/status/remotecontrolactivationstate - Remote control activation state
     * https://developer.home-connect.com/docs/api/status/remotestartallowancestate - Remote start allowance state
     * https://developer.home-connect.com/docs/api/status/localcontrolstate - Local control state
     * https://developer.home-connect.com/docs/status/operation_state - Operation state
     * https://developer.home-connect.com/docs/status/door_state - Door state
     *
     * Several more device-specific states can be found at https://developer.home-connect.com/docs/api/status/remotecontrolactivationstate.
     *
     * Example return value:
     *
     *  {
     *    "key": "BSH.Common.Status.OperationState",
     *    "value": "BSH.Common.EnumType.OperationState.Ready"
     *  }
     */
    instance.getSingleStatus = { haId, statusKey, closure ->
        log.info("Retrieving the status '${statusKey}' of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/status/${statusKey}") { resp ->
            closure.call(resp.data)
        }
    };

    /**
     * Get a list of available settings
     *
     * Get a list of available setting of the home appliance.
     * Further documentation can be found here:
     *
     *  https://developer.home-connect.com/docs/settings/power_state - Power state
     *  https://developer.home-connect.com/docs/api/settings/fridgetemperature - Fridge temperature
     *  https://developer.home-connect.com/docs/api/settings/fridgesupermode - Fridge super mode
     *  https://developer.home-connect.com/docs/api/settings/freezertemperature - Freezer temperature
     *  https://developer.home-connect.com/docs/api/settings/freezersupermode - Freezer super mode
     *
     * Example return value:
     *
     * [
     *   {
     *     "key": "BSH.Common.Setting.PowerState",
     *     "value": "BSH.Common.EnumType.PowerState.On"
     *   },
     *   {
     *     "key": "Refrigeration.FridgeFreezer.Setting.SuperModeFreezer",
     *     "value": true
     *   }
     * ]
     */
    instance.getSettings = { haId, closure ->
        log.info("Retrieving the settings of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/settings") { resp ->
            closure.call(resp.data.settings)
        }
    };

    /**
     * Get a specific setting
     *
     *
     * Example return value:
     *
     * {
     *   "key": "BSH.Common.Setting.PowerState",
     *   "value": "BSH.Common.EnumType.PowerState.On",
     *   "type": "BSH.Common.EnumType.PowerState",
     *   "constraints": {
     *     "allowedvalues": [
     *       "BSH.Common.EnumType.PowerState.On",
     *       "BSH.Common.EnumType.PowerState.Standby"
     *     ],
     *     "access": "readWrite"
     *   }
     * }
     */
    instance.getSetting = { haId, settingsKey, closure ->
        log.info("Retrieving the setting '${settingsKey}' of Home Appliance '$haId' from Home Connect")
        apiGet("/api/homeappliances/${haId}/settings/${settingsKey}") { resp ->
            closure.call(resp.data)
        }
    };

    /**
     * Get stream of events for one appliance
     *
     * NOTE: This can only be done from within a device driver. It will not work within an app
     */
    instance.connectDeviceEvents = { haId, interfaces -> 
        log.info("Connecting to the event stream of Home Appliance '$haId' from Home Connect")
        interfaces.eventStream.connect(
            "${apiUrl}/api/homeappliances/${haId}/events",
            [ headers: ([ 'Accept': 'text/event-stream' ] << authHeaders())])
    }

    /**
     * Get stream of events for all appliances 
     *
     * NOTE: This can only be done from within a device driver. It will not work within an app
     */
    instance.connectEvents = { interfaces -> 
        log.info("Connecting to the event stream of all Home Appliances from Home Connect")
        interfaces.eventStream.connect(
            "${apiUrl}/api/homeappliances/events",
            [ headers: ([ 'Accept': 'text/event-stream' ] << authHeaders())])
    }

    return instance;
}

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

definition(
    name: 'Home Connect Integration',
    namespace: 'wattos.homeconnect',
    author: 'Filip Wieladek',
    description: 'Integrates with Home Connect',
    category: 'My Apps',
    iconUrl: '',
    iconX2Url: '',
    iconX3Url: ''
)

@Field HomeConnectAPI = HomeConnectAPI_create(oAuthTokenFactory: {return getOAuthAuthToken()});
@Field URIUtils = URIUtils_create();

//  ===== Settings =====
private getClientId() { settings.clientId }
private getClientSecret() { settings.clientSecret }

//  ===== Lifecycle methods ====
def installed() {
    log.info "installing Home Connect"
    synchronizeDevices();
}

def uninstalled() {
    log.info "uninstalled Home Connect"
    deleteChildDevicesByDevices(getChildDevices());
}

def updated() {	
    log.info "updating with settings"
    synchronizeDevices();
}

//  ===== Pages =====
def getHomeConnectAPI() {
    return HomeConnectAPI;
}

//  ===== Pages =====
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

// ==== App behaviour ====

def synchronizeDevices() {
    def childDevices = getChildDevices();
    def childrenMap = childDevices.collectEntries {
        [ "${it.deviceNetworkId}": it ]
    };

    for (homeConnectDeviceId in settings.devices) {
        def hubitatDeviceId = homeConnectIdToDeviceNetworkId(homeConnectDeviceId);

        if (childrenMap.containsKey(hubitatDeviceId)) {
            childrenMap.remove(hubitatDeviceId)
            continue;
        }

        device = addChildDevice('wattos.homeconnect', 'Homeconnect Dishwasher', hubitatDeviceId);
        device.updateDataValue("haId", homeConnectDeviceId);
    }

    deleteChildDevicesByDevices(childrenMap.values());
}

def deleteChildDevicesByDevices(devices) {
    for (d in devices) {
        deleteChildDevice(d.deviceNetworkId);
    }
}

def homeConnectIdToDeviceNetworkId(haId) {
    return "wattos.homeconnect:${haId}"
}

//TODO: Move out into helper library
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
        'scope': 'IdentifyAppliance Monitor Settings',
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

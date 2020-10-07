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

    def apiGet = { path, closure ->
        log.debug("API Get Request to Home Connect with path $path and query $query")
        return httpGet(uri: apiUrl,
                'path': path,
                'headers': ['Authorization': "Bearer ${oAuthTokenFactory()}"]) { resp -> 
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
        log.info("Retrieving Home Appliance '$id' from Home Connect")
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
        log.info("Retrieving All Programs of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs") { resp ->
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
        log.info("Retrieving All Programs of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/available") { resp ->
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
        log.info("Retrieving the '${programKey}' Program of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/available/${programKey}") { resp ->
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
        log.info("Retrieving the active Program of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/active") { resp ->
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
        log.info("Retrieving the active Program Options of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/active/options") { resp ->
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
        log.info("Retrieving the active Program Option '${optionKey}' of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/active/options/${optionKey}") { resp ->
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
        log.info("Retrieving the selected Program of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/selected") { resp ->
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
        log.info("Retrieving the selected Program Options of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/selected/options") { resp ->
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
        log.info("Retrieving the selected Program Option ${optionKey} of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/programs/selected/options/${optionKey}") { resp ->
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
        log.info("Retrieving the status of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/status") { resp ->
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
        log.info("Retrieving the status of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/status/${statusKey}") { resp ->
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
        log.info("Retrieving the settings of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/settings") { resp ->
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
        log.info("Retrieving the settings of Home Appliance '$id' from Home Connect")
        apiGet("/api/homeappliances/${haid}/settings/${settingsKey}") { resp ->
            closure.call(resp.data)
        }
    };

    return instance;
}

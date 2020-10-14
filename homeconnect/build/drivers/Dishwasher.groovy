import groovy.json.JsonSlurper

metadata {
    definition(name: "Home Connect Dishwasher", namespace: "wattos.homeconnect", author: "wattos@gmail.com") {
        capability "Sensor"
        capability "Initialize"


        // BSH.Common.Status.RemoteControlActive
        // This status indicates whether the allowance for remote controlling is enabled.
        attribute "remoteControlActive", "enum", ["on", "off"]

        // BSH.Common.Status.RemoteControlStartAllowed
        // This status indicates whether the remote program start is enabled. 
        // This can happen due to a programmatic change (only disabling), 
        // or manually by the user changing the flag locally on the home appliance, 
        // or automatically after a certain duration - usually 24 hours.
        attribute "remoteControlActive", "enum", ["on", "off"]

        // BSH.Common.Status.OperationState
        // This status describes the operation state of the home appliance. 
        attribute "remoteControlActive", "enum", [
            // Key: BSH.Common.EnumType.OperationState.Inactive
            // Description: Home appliance is inactive. It could be switched off or in standby.
            "Inactive",

            // Key: BSH.Common.EnumType.OperationState.Ready
            // Description: Home appliance is switched on. No program is active.
            "Ready",

            // Key: BSH.Common.EnumType.OperationState.DelayedStart
            // Description: A program has been activated but has not started yet.
            "Delayed Start",

            // Key: BSH.Common.EnumType.OperationState.Run
            // Description: A program is currently active.
            "Run",

            // Key: BSH.Common.EnumType.OperationState.Pause
            // Description: The active program has been paused.
            "Pause",

            // Key: BSH.Common.EnumType.OperationState.ActionRequired
            // Description: The active program requires a user interaction.
            "Action required",

            // Key: BSH.Common.EnumType.OperationState.Finished
            // Description: The active program has finished or has been aborted successfully.
            "Finished",

            // Key: BSH.Common.EnumType.OperationState.Error
            // Description: The home appliance is in an error state.
            "Error",

            // Key: BSH.Common.EnumType.OperationState.Aborting
            // Description: The active program is currently aborting.
            "Aborting",
        ]

        // BSH.Common.Status.DoorState
        // This status describes the state of the door of the home appliance. 
        // A change of that status is either triggered by the user operating 
        // the home appliance locally (i.e. opening/closing door) or 
        // automatically by the home appliance (i.e. locking the door).
        //
        // Please note that the door state of coffee machines is currently 
        // only available for American coffee machines. 
        // All other coffee machines will be supported soon.
        attribute "doorState", "enum", [
            //  Key: BSH.Common.EnumType.DoorState.Open
            // Description: The door of the home appliance is open.
            "Door open",

            // Key: BSH.Common.EnumType.DoorState.Closed
            // Description: The door of the home appliance is closed but not locked.
            "Door closed",

            //  Key: BSH.Common.EnumType.DoorState.Locked
            // Description: The door of the home appliance is locked.
            "Door locked",
        ]

        attribute "activeProgram", "string"

        attribute "powerState", "enum", [
            // Key: BSH.Common.EnumType.PowerState.Off
            // Description: The home appliance switched to off state but can 
            // be switched on by writing the value BSH.Common.EnumType.PowerState.
            // On to this setting.
            "Off",

            // Key: BSH.Common.EnumType.PowerState.On
            // Description: The home appliance switched to on state. 
            // You can switch it off by writing the value BSH.Common.EnumType.PowerState.Off 
            // or BSH.Common.EnumType.PowerState.Standby depending on what is supported by the appliance.
            "On",

            //  Key: BSH.Common.EnumType.PowerState.Standby
            // Description: The home appliance went to standby mode.
            // You can switch it on or off by changing the value of this setting appropriately.
            "Stand-by"
        ]
    }
}

void initialize() {
    intializeStatus();
}

void installed() {
    intializeStatus();
}


void updated() {
    intializeStatus();
}

void uninstalled() {
    interfaces.eventStream.close();
}

void parse(String message) {
    log.debug "Received eventstream message: ${message}"
}

void eventStreamStatus(String description) {
    log.debug "Received eventstream status message: ${description}"
}

void intializeStatus() {
    def haId = getDataValue("haId")
    log.info "Initializing the status of the device ${haId}"

    parent.getHomeConnectAPI().getStatus(haId) { status ->
        log.info "Status received: ${status}"
    }

    parent.getHomeConnectAPI().getSettings(haId) { status ->
        log.info "Status received: ${status}"
    }

    try {
        parent.getHomeConnectAPI().getActiveProgram(haId) { status ->
            log.info "Status received: ${status}"
        }
    } catch (e) {
        // no active program
        if(isStateChange(device, "activeProgram", "")) {
            sendEvent(name: "activeProgram", value: "newStat", descriptionText: "Active Program changed to: ${newStat}", displayed: true, isStateChange: true)
        }
    }

    interfaces.eventStream.close();
    parent.getHomeConnectAPI().connectDeviceEvents(haId, interfaces);
}

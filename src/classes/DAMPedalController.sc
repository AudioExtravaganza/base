/*********************************************************************
Extension: DAM Pedal Controller
Authors: Mason Sidebottom
Purpose: Provides backend functionallity for triggering effects
Date: Feb. 2019
*********************************************************************/

DAMPedalController {
	// Dictionary of functions
	var <> receivers;

	// Array of control busses
	var <> knobs;

	// Master groups for pedal
	var <> inGroup;
	var <> outGroup;

	// The active Scences
	var <> currentScene;

	var <> scenes;

	var <> inBus;
	var <> outBus;
	/******************************************************************
	Build
		Basic constructor. Must be explicitly called after an instance
		is created
		Parameters: parent (should be the server)
	*******************************************************************/
	build {
		arg parent;
		this.inGroup = Group.new(parent);
		this.outGroup = Group.after(this.inGroup);
		this.outBus = [0,1];
		this.inBus = [0,1];
		this.receivers = Dictionary(0);
		this.knobs = Array.fill(4, {arg i; Bus.control(parent, 1)});
		this.scenes = Dictionary(0);
		this.bindOSC();
	}

	/******************************************************************
	Add Scene
		Adds a DAM Scene to the Pedal Controller
		Parameters: name, string to be used as key
					scene a DAMScene
	*******************************************************************/
	addScene {
		arg name, scene;
		this.scenes.add(name -> scene);
	}

	/******************************************************************
	Remove Scene
		Removes a DAM Scene from the Pedal Controller
		Parameters: name, string to be used as key

		! Assumes there is a scene associated with the key
	*******************************************************************/
	removeScene {
		arg name;
		this.scenes.removeAt(name);
	}

	/******************************************************************
	Set Input Bus
		Changes the entire pedals main input
		Parameters: New input bus
	*******************************************************************/
	setInputBus {
		arg bus;
		this.inBus = bus;
		if(this.currentScene != nil){
			this.inBus.println;
			this.currentScene.updateInputBus(this.inBus);
		}
	}

	/******************************************************************
	Set Output Bus
		Changes the entire pedals output
		Parameters: New output bus
	*******************************************************************/
	setOutputBus {
		arg bus;
		this.outBus = bus;
		if(this.currentScene != nil){
			this.currentScene.updateOutBus(this.outBus);
		}
	}

	/******************************************************************
	Update Switch
		Alerts current scene of new footswitch value
		Parameters: state, the new switch state
					index, index of the switch
	*******************************************************************/
	updateSwitch {
		arg state, index;
		if(this.currentScene != nil){
			this.currentScene.updateChain(state, index);
		};
	}

	/******************************************************************
	Update Scene
		Starts up a new scene if it should be started
		Parameters: name of new scene
	*******************************************************************/
	updateScene {
		arg name;
		// Check that a scene is running
		if(this.currentScene != nil){
			// If we are trying to launch the same scene that is
			//   already running, dont
			if(this.currentScene.getName() == name){
				^-1;
			}{
				// Otherwise, kill it (frees things that are allocated)
				//	 when scene is triggered'

				this.currentScene.kill;

			};
		};

		// Update current scene
		this.currentScene = this.scenes.at(name);

		// Trigger the scene
		this.currentScene.trigger(this.inGroup, this.knobs, this.inBus, this.outBus);
	}

	/******************************************************************
	Bind OSC
		Adds listener for a message with start as name, use as handler
			name as well

		Parameters: name, the beggining of the message ex '/Knob 1'
					type, 'k' -> Knob, 'p' -> pedal, 'm' -> menu
					num, index of control (ex pedal 1 -> 1)
	*******************************************************************/
	bindOSC {
		// Add the function to the dictionary
		this.receivers.add("GUI-Reciever" -> {
			arg msg, time, addr;
			// If this is the right handler
			msg.println;
			// Switch on the type
			switch(msg[0],
				// If knob, update knob bus
				'/knob', {this.knobs[msg[1]].set(msg[2]);},

				// If pedal, update pedal state
				'/pedal', {this.updateSwitch(msg[2], msg[1]);},

				// If menu, update menu string
				'/menu', {this.updateScene(msg[1]);}
			);



		});

		// Add this new function to the OSC receivers
		thisProcess.addOSCRecvFunc(this.receivers.at("GUI-Reciever"));
	}

	/******************************************************************
	Remove OSC
		Removes listener for a message with start as name

		Parameters: name of handler

		! Assumes handler with name provided exists
	*******************************************************************/
	removeOSC {
		arg name;

		// Remove from OSC Recievers
		thisProcess.removeOSCRecvFunc(this.receivers.at(name));

		// Remove from local Dict
		this.receivers.removeAt(name);
	}

	/******************************************************************
	Free
		Frees all allocated members
	*******************************************************************/
	free {

		// For all recievers
		this.receivers.keys.do{
			arg k;

			// Remove the OSC binding (frees in function)
			this.removeOSC(k);
		};

		// For all knob control busses
		this.knobs.do{
			arg item;

			// Free
			item.free;
		}
	}
}

/*********************************************************************
DAM Scene
	Class that contains effects and effect chains.
*********************************************************************/
DAMScene {

	// Local refference to the Knob busses
	var <> knobs;

	// Array of synthdef names
	var <> effects;

	// Name of this instance
	var <> name;

	// Array of active synths
	var <> synths;

	// Individual groups
	var <> group;

	// Array of local audio busses (stereo)
	var <> busses;
	var <> chains;

	/******************************************************************
	Prebuild
		Instantiates basic members that are not pertinent to audio
		processing.

		This should be called before sending to the pedal controller
	*******************************************************************/
	prebuild {
		arg name;

		// Set name
		this.name = name;

		// Init knobs and synth to null
		this.knobs = nil;
		this.synths = nil;

		// Set effect names to tombstone values
		this.effects = Array.fill(3, {arg i; -1});

		this.chains = Array.fill(3, {arg i; DAMChain.new()});
	}

	/******************************************************************
	Get name
		Gets name of scene
	*******************************************************************/
	getName {
		^this.name;
	}

	/******************************************************************
	Trigger
		Allocates and sets up busses and synth routing

		Params: node  -> parent group, will place new group below this
							generall the input group
				knobs -> Array of 4 control busses
				inputbus -> Input audio signal (as audio bus)
				outputBus -> Output audio bus)

		! Do not call this outside of the pedal controller. This should
		! only be called by DAMPedalController::updateScene
	*******************************************************************/
	trigger {
		arg node, knobs, inputBus, outputBus;

		// Add group after node
		// 	it is expexcted  that node is the input group
		this.group = Group.after(node);

		// Do not free busses[0] or busses[3]
		this.busses = [Bus.audio(Server.local, 2), Bus.audio(Server.local, 2), Bus.audio(Server.local, 2),  Bus.audio(Server.local, 2)];
		// Reference to knobs !Do not free in this class
		this.knobs = knobs;
		this.busses.postln;
		// Create array of size 3

		// Create chain using busses in -> bus[0] -> bus[1] -> bus[2] -> bus[3] -> out
		this.synths = [0, 0];
		this.synths[0] = Synth.new("Route", [\in, inputBus, \out, this.busses[0]], this.group);
		this.synths[1] = Synth.tail(this.group, "Route", [\in, this.busses[3], \out, outputBus]);

		this.chains[0].init(this.busses[0], this.busses[1], this.knobs);
		// Off creates a route between local in and out busses of chain
		this.chains[0].off(this.synths[0]);

		this.chains[1].init(this.busses[1], this.busses[2], this.knobs);

		// Send chain 1 the previous chains node
		this.chains[1].off(this.chains[0].requestNode());

		this.chains[2].init(this.busses[2], this.busses[3], this.knobs);

		// Send chain 2 the previous chains node
		this.chains[2].off(this.chains[1].requestNode());



	}

	/******************************************************************
	Update Input Bus
		Remaps input bus
		Param: bus -> new bus
	*******************************************************************/
	updateInputBus{
		arg bus;
		this.busses[0] = bus;
	}

	/******************************************************************
	Update Output Bus
		Remaps output bus
		Param: bus -> new bus
	*******************************************************************/
	updateOutBus {
		arg bus;
		this.busses[3] = bus;
	}


	/******************************************************************
	Update Switch !!!! DEPRACATED REMOVE AFTER REGRESSION TESTING
		Toggles individual effects/effect chains
		Params: state -> 1 (on) or 0 (off)
		index -> index of the switch [0,1,2]
	*******************************************************************/
	updateSwitch {
		arg state, index;
		var args;

		// Make sure that synths are defined
		if(this.synths == nil){
			^-1;
		};

		// Make sure that there is a synthdef for this switch
		if(this.effects[index] == -1){
			^-1;
		};

		// Free previous synth (there should always be a synth running
		//	at each index, because of route);
		this.synths[index].free;

		// Predefine args (same for every call)
		args = [\in, this.busses[index], \out, this.busses[index+1]];

		// If starting a new synth;
		if(state == 1){

			// If this is the first synth, set it before the 2nd synth
			if(index == 0){
				this.synths[index] = Synth.before(this.synths[index + 1], this.effects[index], args);

			// Otherwise, set is after the previous synth
			}{
				this.synths[index] = Synth.after(this.synths[index -1], this.effects[index], args);

			};

			// Map knobs to the synth
			// TODO: Allow mapping to be specified per each synth
			this.synths[index].map(
				\p1, this.knobs[0].index,
				\p2, this.knobs[1].index,
				\p3, this.knobs[2].index,
				\p4, this.knobs[3].index
			);

		// If stopping a synth
		}{
			// If this is the first synth, route the in->out before the 2nd synth
			if(index == 0){
				this.synths[index] = Synth.before(this.synths[index + 1], "Route", args);

			// If this is the first synth, route the in->out after the previous synth
			}{
				this.synths[index] = Synth.after(this.synths[index - 1], "Route", args);
			};
		};
	}

	/******************************************************************
	Update Chain
		Toggles individual effects/effect chains
	Params: state -> 1 (on) or 0 (off) or 1 (hold)
			index -> index of the switch [0,1,2]
	*******************************************************************/
	updateChain {
		arg state, index;
		var node = nil;

		//Verify we have a chain to update
		if(chains[index] == -1){
			^-1;
		};

		// Find the node for the new synth to start after
		if(index == 0){
			node = this.synths[0];
		}{
			node = this.chains[index - 1].requestNode();
		};

		// If a node exists, update the state.
		if(node != nil){
			this.chains[index].updateState(state, node);
		};
	}

	/******************************************************************
	Set Effect
		Sets effect to specific index;
		Params: effect -> name of some synth def
				index -> index to store the effect at, direct mapping to
						 the switches
	*******************************************************************/
	setEffect {
		arg effect, index;
		this.effects[index] = effect;
	}


	setChain {
		arg chain, index;
		this.chains[index] = chain;
	}

	/******************************************************************
	Kill
		Frees all resources allocated by trigger. Allows the same scene
			to be triggered again later without freeing it all.
	*******************************************************************/
	kill {
		// Free the chains
		this.chains.do{
			arg item, i;
			if(item != nil){
				item.kill;
			}
		};
		// Free the busses
		this.busses.do{
			arg item, i;
			if(item != nil){
				item.free;
			}
		};

		// Free all synths (master in and out routes)
		this.synths[0].free;
		this.synths[1].free;

		// Get rid of this group
		this.group.free;

	}
}



/*********************************************************************
DAM Chain
	Class that controls firing hold and tap behaviors
*********************************************************************/
DAMChain{

	// Local in and out busses
	var <> busIn;
	var <> busOut;

	// Name of synthdef for hold action
	var <> holdAction;

	// Name of synthdef for tap actions
	var <> tapAction;

	// Current state
	var <> state;

	// Current synth
	var <> synth;

	// Parameters for each action
	var <> holdParams;
	var <> tapParams;

	// Control busses of knob values
	var <> knobs;


	/******************************************************************
	Pre Build
		Call to setup defualt values of the chain
		Params: tapAction -> Name of the synthdef for the tapAction
				tapParams -> Array of boolean values, each index
							corresponds to a knob.
				holdAction -> Name of the synthdef for the tapAction
				holdParams -> Array of boolean values, each index
							corresponds to a knob.
	*******************************************************************/
	preBuild {
		arg tapAction = nil, tapParams = [false, false, false, false], holdAction = nil, holdParams = [false, false, false, false];

		// Default all values from parms
		this.holdAction = holdAction;
		this.tapAction = tapAction;
		this.synth = nil;
		this.holdParams = holdParams;
		this.tapParams = tapParams;
	}

	/******************************************************************
	Init
		Initializes the signal processing routes
		Params: busIn -> Local bus in
				busOut -> Local bus out
				Knobs -> Array of control busses with knob values

		! Do not call this explicitly, it should only be called from
		! DAMScene class to handle routing setup
	*******************************************************************/
	init {
		arg busIn, busOut, knobs;

		// Assign member values
		this.busIn = busIn;
		this.busOut = busOut;
		this.knobs = knobs;
		this.state = 0;
	}

	/******************************************************************
	Set Tap
		Manually set tap action
		Params: SynthName -> Name of the synthdef for the tapAction
				Params -> Array of boolean values, each index
							corresponds to a knob.
	*******************************************************************/
	setTap {
		arg synthName, params = [false, false, false, false];
		this.tapAction = synthName;
		this.tapParams = params;
	}


	/******************************************************************
	Set Hold
		Manually set hold action
		Params: SynthName -> Name of the synthdef for the tapAction
				Params -> Array of boolean values, each index
							corresponds to a knob.
	*******************************************************************/
	setHold {
		arg synthName, params = [false, false, false, false];
		this.holdAction = synthName;
		this.holdParams = params;
	}


	/******************************************************************
	Update state
		Alerts the current chain of the new state
		Params: State -> New state to change to
							0 -> off
							1 -> Tap
							2 -> Hold
				Node -> the node to spawn the synth after

		! Should only be called by DAMScene
	*******************************************************************/
	updateState{
		arg state, node;
		// Check that there are actions, if not, this is just a direct out
		if(this.holdAction == nil && this.tapAction == nil){
			^0; // Just a direct through chain.
		};

		// If going from on/hold to off
		if(this.state >= 1  && state == 0){
			this.off(node);
		};

		// If off going to tap
		if(this.state == 0 && state == 1){
			this.tap(node);
		};

		// If off going to tap
		if(this.state == 0 && state == 2){
			this.hold(node);
		}

	}


	/******************************************************************
	Request Node
		Gets the current synth, should only be used for node tree
			actions. I.e. dont modify the synth
		Returns: the current synth node
	*******************************************************************/
	requestNode {
		^this.synth;
	}


	/******************************************************************
	Off
		Turns off current synth, then routes the in -> out
		Params: Node -> Node to spawn the new synth after
	*******************************************************************/
	off {
		arg node;
		// Set state to 0
		this.state = 0;

		// Free synth if we should
		if(this.synth != nil){
			this.synth.free;
		};

		// Spawn new route synth after the node
		this.synth = Synth.after(node, "Route", [\in, this.busIn, \out, this.busOut]);
	}

	/******************************************************************
	Tap
		Turns off current synth, then toggles tap action
		Params: Node -> Node to spawn the new synth after
	*******************************************************************/
	tap {
		arg node;
		var i, params;


		// Verify we have an action
		if(this.tapAction == nil){
			^-1;
		};

		// Free synth if we should
		if(this.synth != nil){
			this.synth.free;
		};


		// Start the synth
		this.synth = Synth.after(node, this.tapAction, [\in, this.busIn, \out, this.busOut]);
		// Map the knobs to the parameters
		i = 0;
		params = [\p1, \p2, \p3, \p4];
		this.tapParams.do{
			arg elem, index;
			if(elem){
				this.synth.map(params[i], this.knobs[index]);
				i = i + 1;
			}
		};

		// Update the state, (if we are this far, we were successful in spawning synth)
		this.state = 1;
	}


	/******************************************************************
	Hold
		Turns off current synth, then starts the hold action
		Params: Node -> Node to spawn the new synth after
	*******************************************************************/
	hold {
		arg node;
		var i, params;


		// Verify there is an action to start
		if(this.holdAction == nil){
			"No hold action".postln;
			^-1;
		};

		// Free synth if we should
		if(this.synth != nil){

			this.synth.free;
		};

		// Start the hold action after the node
		this.synth = Synth.after(node, holdAction, [\in, this.busIn, \out, this.busOut]);
		// Bind knobs to parameters
		i = 0;
		params = [\p1, \p2, \p3, \p4];
		this.holdParams.do{
			arg elem, index;
			if(elem){
				this.synth.map(params[i], this.knobs[index]);
				i = i + 1;
			}
		};

		// Update the state, we were successful in starting synth
		this.state = 2;
	}

	/******************************************************************
	Free
		Frees local synth
	*******************************************************************/
	kill{
		if(this.synth != nil){
			this.synth.free;
			this.synth = nil;
		}
	}
}


/*********************************************************************
DAM Looper
	Class that controls firing hold and tap behaviors
*********************************************************************/
DAMLooper : DAMChain {

	var <>max_loop = 30; // Maximum loop length in seconds
	var <>channels = 2;

	var <>max_samples;
	var <>clockBus;
	var <>buffer;
	var <>loopLen, <>loopSamples;
	var <>recSynth, <>playSynth;

	// Initialize looper class
	init{
		arg busIn, busOut, knobs = [];

		this.busIn = busIn;
		this.busOut = busOut;

		this.clockBus = Bus.control();

		this.max_samples = max_loop * Server.local.sampleRate;

		"Finished initializing looper".postln;
	}

	// Create a new loop
	newLoop{
		arg node;
		var toFree;
		// Set the initial buffer
		this.buffer = Buffer.new(Server.local, max_samples, channels);

		// Create the recording synth
		this.recSynth = Synth.new(\recLoop, [
			inBus: this.busIn,
			clockBus: this.clockBus,
			buffer: this.buffer,
			loopSamples: max_samples
		]);

		// Handle the synth finishing
		OSCFunc({ | msg, time |
			var tempBuf;

			// Get the amount of time recorded
			this.loopLen = msg[3];
			// Convert recorded time to frames
			this.loopSamples = (this.loopLen * Server.local.sampleRate).trunc;

			// Copy the recorded frames into a buffer of the right length
			tempBuf = Buffer.alloc(Server.local, this.loopSamples, channels, {
				arg buf;
				this.buffer.copyMsg(buf, 0, 0, this.loopSamples);


				// Free the current buffer
				toFree = this.buffer;
				// Replace with the newly copied buffer
				this.buffer = tempBuf;
				// Start playback
				// this.playLoop(node);
			});
			toFree.free;
		},'/tr', Server.local.addr, nil, [this.recSynth.nodeID, 0]).oneShot;

	}

	// Overdub an existing loop
	dubLoop{
		arg node;
		if( this.recSynth.isNil ) {
			this.recSynth = Synth.new(\recLoop, [
				inBus: this.busOut,
				clockBus: this.clockBus,
				buffer: this.buffer,
				loopSamples: this.loopSamples
			]);
		}
	}

	// Stop a currently running record
	stopRec{
		if( this.recSynth.notNil ){
			this.recSynth.set(\gate, 0);
		}
	}

	// Start playback on a loop
	playLoop{
		arg node;
		if( this.playSynth.isNil){
			this.buffer.postln;
			this.playSynth = Synth.after(node, \playLoop, [
				outBus: this.busOut,
				clockBus: this.clockBus,
				buffer: this.buffer,
				loopSamples: this.loopSamples
			]);
		}
	}

	// Stop loop playback
	stopLoop{
		if( this.playSynth.notNil ){
			this.playSynth.free;
			this.playSynth = nil;
		}
	}

	/******************************************************************
	Update state
		Alerts the current chain of the new state
		Params: State -> New state to change to
							0 -> off
							1 -> Tap
							2 -> Hold
				Node -> the node to spawn the synth after

		! Should only be called by DAMScene
	*******************************************************************/
	updateState{
		arg state, node;
		state.postln;
		// On hold
		if( state == 2 ) {
			// Start a new loop if none exists
			if( this.playSynth.isNil ){
				this.newLoop(node);
			}{
			// Otherwise overdub
				this.dubLoop(node);
			};

			this.state = 2;
		};

		// On tap
		if( state == 1 ) {
			// Play the loop
			this.playLoop(node);

			this.state = 1;
		};

		// On release
		if( state == 0 ) {

			// If we're recording
			if( this.state == 2 ) {
				// Stop recording
				this.stopRec(node);
			};
			// If we're playing
			if( this.state == 1 ) {
				// Stop playing
				this.stopLoop(node);
			};

			this.state = 0;
		};
		"Update state override working".postln;

	}

	/******************************************************************
	Free
		Frees local synth
	*******************************************************************/
	kill{
		if(this.playSynth != nil){
			this.playSynth.free;
			this.playSynth = nil;
		};
		if(this.recSynth != nil) {
			this.recSynth.free;
			this.recSynth = nil;
		};
		if(this.buffer != nil){
			this.buffer.free;
			this.buffer = nil;
		};
	}
}

(
Server.default.options.numBuffers = 2048 * 32;

~src = thisProcess.nowExecutingPath.dirname;

// Loads all scenes into a list called ~scenes
(~src ++ "/resources/expo-scenes.scd").load;

// Loads all synthdefs for effects into server
(~src ++ "/resources/expo-effects.scd").load;


s.waitForBoot({
	// Debugging on, send data over OSC
	g = DAMGUI.new();
	g.build(true, true);

	// Setup the server
	c = DAMPedalController.new();
	c.build(s);

	~scenes.do{
		arg item;
		// Add to the GUI
		g.addScene(item.getName());

		// Add scene to the controller
		c.addScene(item.getName(), item);
	};

	// Send all GUI info to the server
	g.forceOSCUpdate;

	// Setup wav file as input, change to false to disable
	if(true){
		// Change this path to point to your WAV file
		~inputWav = ~src ++ "/guitar_riff.wav".standardizePath.postln ;
		~wavBus = Bus.audio(Server.local, 2);
		~wavBuf = Buffer.read(s, ~inputWav); // remember to free the buffer later.
		~wavSynth = Synth.new("PlayBuffer", [\buffer, ~wavBuf,  \out, ~wavBus], c.inGroup);
		c.setInputBus(~wavBus);
	};

	// Free the controller, then stop the server.
	g.addCloseAction({c.free; s.quit;});
});
)

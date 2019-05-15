(
Server.default.options.numBuffers = 2048 * 32;

~src = thisProcess.nowExecutingPath.dirname;

// Loads all scenes into a list called ~scenes
(~src ++ "/resources/scenes.scd").load;

// Loads all synthdefs for effects into server
(~src ++ "/resources/effects.scd").load;


s.waitForBoot({
	// Setup the server
	c = DAMPedalController.new();
	c.build(s);

	// For all scenes in scenes.scd
	~scenes.do{
		arg item;
		// Add scene to the controller
		c.addScene(item.getName(), item);

		(c.currentScene == nil).if {
			c.updateScene(item.getName());
		}
	};

	// Setup wav file as input, change to false to disable
	if(true){
		// Change this path to point to your WAV file
		~inputWav = ~src ++ "/guitar_riff.wav".standardizePath.postln ;
		~wavBus = Bus.audio(Server.local, 2);
		~wavBuf = Buffer.read(s, ~inputWav); // remember to free the buffer later.
		~wavSynth = Synth.new("PlayBuffer", [\buffer, ~wavBuf,  \out, ~wavBus], c.inGroup);
		c.setInputBus(~wavBus);
	};
});
)

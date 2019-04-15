SynthDef.new(\PlayBuffer, {
	arg buffer = 0, out;
	var source;

	// Set source to repeat audio from buffer
	source = PlayBuf.ar(2, buffer, BufRateScale.kr(buffer), loop: 1.0);

	// Output the source to the audio
	//    bus from args
	Out.ar(out, source);

}).add; // Add synth to server



// This synth def is required by DAMScenes
SynthDef("Route", {
	arg in, out;
	var signal;
	signal = In.ar(in, 2);
	Out.ar(out, signal);
}).add;

// Arbitrary signal modification
SynthDef("Reverb_Base", {
	arg in, out, p1, p2, p3, p4;
	var signal;

	signal = In.ar(in, 2);

	// Meaningless operations
	signal = FreeVerb.ar(signal, p1, p2, 0.5, 1);

	Out.ar(out, signal);
}).add;  // Add to the server !!!!! Dont forget to do this

SynthDef("PulseSig", {
	arg in, out, p1, p2, p3, p4;
	var signal;

	signal = In.ar(in, 2);
	signal = signal * LFPulse.kr(2);
	Out.ar(out, signal);
}).add;


SynthDef.new('Distort', {
	arg in, out, p1, p2, p3, p4;
	var signal;

	signal = In.ar(in, 2);
	signal = signal * 4 * (1 + p1);
	signal = Clip.ar(signal, 0, 1);

	Out.ar(out, signal);

}).add;

SynthDef.new('DAMLPF', {
	arg in, out, p1, p2 = 1, p3 = 0.5, p4;
	var signal, freq;
	freq = 80 + (p1 * 1600);
	signal = In.ar(in, 2);
	signal = LPF.ar(signal, freq, 1, 0);
	Out.ar(out, signal);

}).add;



SynthDef( \recLoop, {
	arg inBus = 0, clockBus = 0, gate = 1, buffer, loopSamples;

	var inSignal;
	var timer = Line.kr(0, loopSamples, loopSamples);
	var trigGate = 1 - (gate + Done.kr(timer));

	// the signal to be recorded
	inSignal = In.ar(inBus, 2);

	// send time used for recording back to language
	SendTrig.kr(trigGate, 0, timer);

	// release synth when recGate closes
	FreeSelf.kr(trigGate);

	// record input signal
	RecordBuf.ar(inSignal, buffer, In.kr(clockBus), recLevel: 1, preLevel: 1);

}).add;

SynthDef( \playLoop, {
	arg outBus, clockBus, buffer, loopSamples;
	var signal = PlayBuf.ar(2, buffer, loop:1);
	// Index the buffer using Phasor
	Out.kr(clockBus, Phasor.kr(0, 1.0, 0.0, loopSamples, 0.0));

	// Send the contents of the buffer to output
	Out.ar(outBus, signal);
}).add;


SynthDef.new('DAMChorus', {
	arg in, out, p1, p2, p3, p4;
	var signal, delay, chorus;
	delay = 0.025 + (p1 * 0.05);
	signal = In.ar(in, 2);

	chorus = Mix.arFill(8 , {
		DelayC.ar(signal, delay, LFNoise1.kr(Rand(0.1, p2),0.01,0.02));
	});

	Out.ar(out, chorus);

}).add;

SynthDef.new('DAMPhaser', {
	arg in, out, p1 = 0.5, p2 = 0.5, p3, p4;
	var signal, delay, phaser;
	delay = 0.005 + (p2 * 0.02);
	signal = In.ar(in, 2);

	phaser = AllpassN.ar(signal, delay, SinOsc.kr(p1, 0, 0.01, 0.01));
	Out.ar(out, phaser);

}).add;



SynthDef.new('DAMCompressor', {
	arg in, out, p1 = 0.5, p2 = 0.5, p3, p4;
	var signal, gain, thresh, cmp;
	gain = 1 + p1 * 2;
	thresh = p2;
	signal = In.ar(in, 2);

	cmp = CompanderD.ar(signal * gain, thresh, 1, 0.5);
	Out.ar(out, cmp);

}).add;

SynthDef.new('DAMDelay', {
	arg in, out, p1 = 0.5, p2 = 0.5, p3, p4;
	var signal, delay;

	signal = In.ar(in, 2);
	delay = CombC.ar(signal, 2, p1 * 2, p2);
	Out.ar(out, delay)
}).add;


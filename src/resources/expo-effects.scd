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
SynthDef(\Route, {
	arg in, out;
	var signal;
	signal = In.ar(in, 2);
	Out.ar(out, signal);
}).add;


SynthDef.new(\DAMLPF, {
	arg in, out, p1, p2 = 1, p3 = 0.5, p4;
	var signal, freq;
	freq = LinExp.kr(p1, 0, 1, 60, 20000);
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


SynthDef.new(\DAMCompressor, {
	arg in, out, p1 = 0.5, p2 = 0.5, p3, p4;
	var signal, gain, thresh, cmp;
	gain = 1 + p1 * 2;
	thresh = p2;
	signal = In.ar(in, 2);

	cmp = CompanderD.ar(signal * gain, thresh, 1, 0.5);
	Out.ar(out, cmp);

}).add;

SynthDef.new(\DAMDelay, {
	arg in, out, p1 = 0.5, p2 = 0.5, p3, p4;
	var signal, delay;

	signal = In.ar(in, 2);
	delay = CombC.ar(signal, 2, p1 * 2, p2);
	Out.ar(out, delay)
}).add;

SynthDef.new(\Reverb, {
	arg in, out, p1 = 0.5, p2 = 0.1, p3, p4;
	var signal, reverb, size, freq;
	size = p2 * 100;
	signal = In.ar(in, 2);
	reverb = JPverb.ar(
		signal,
		p1 * 10,
		0,
		size,
		0.707,
		0.1,
		freq
	);
	Out.ar(out, (reverb + signal) * 0.5);
}).add;



SynthDef.new(\Reverb, {
	arg in, out, p1 = 0.5, p2 = 0.1, p3, p4;
	var signal, reverb, size, freq;
	size = p2 * 100;
	signal = In.ar(in, 2);
	reverb = JPverb.ar(
		signal,
		p1 * 10,
		0,
		size,~tempChain = DAMChain.new();
~tempChain.preBuild('GreyHoleEcho', [true, true, false, false]);
~temp.setChain(~tempChain, 0);
		0.707,
		0.1,
		freq
	);
	Out.ar(out, (reverb + signal) * 0.5);
}).add;

SynthDef.new(\GreyHoleEcho, {
	arg in, out, p1 = 0.5, p2 = 0.1, p3, p4;
	var signal, echo, delay;
	delay = p1 * 20;

	signal = In.ar(in, 2);
	echo = Greyhole.ar(signal, delay, 0, 1.0, 0.707, p2);
	Out.ar(out, (echo + signal) * 0.5);
}).add;


SynthDef.new(\Ladder, {
	arg in, out, p1 = 0.5, p2 = 1, p3, p4;
	var signal, ladder, freq, res;
	freq = LinExp.kr(p1, 0, 1, 60, 160000);
	res = p2;
	signal = In.ar(in, 2);
	ladder = MoogLadder.ar(signal, freq, res);
	Out.ar(out, ladder);
}).add;

SynthDef.new(\Decimate, {
	arg in, out, p1 = 0.5, p2 = 0, p3, p4;
	var signal, decimate, bits;
	// bits = LinLin.kr(p1, 0, 1, 2, 32);
	signal = In.ar(in, 2);
	decimate = Decimator.ar(signal, 44100.0, LinLin.kr(p1, 0, 1, 2, 16));
	Out.ar(out, decimate);
}).add;


SynthDef.new(\Friction, {
	arg in, out, p1 = 0.5, p2 = 0, p3, p4;
	var signal, friction;
	signal = In.ar(in, 2);
	friction = Friction.ar(signal, LinExp.kr(p1, 0, 1, 2e-10, 9e-1), mass:8.05);
	Out.ar(out, friction);
}).add;

SynthDef(\Flanger, {
	arg in, out, p1 = 0.01, p2 = 0.04, p3 = 0.04, p4;
    var input, maxdelay, maxrate, dsig, mixed, local;
    maxdelay = 0.013;
    maxrate = 10.0;
	input = In.ar(in);
    dsig = AllpassL.ar( // the delay (you could use AllpassC (put 0 in decay))
        input,
        maxdelay * 2,
        LFPar.kr( // very similar to SinOsc (try to replace it) - Even use LFTri
            p1 * maxrate,
            0,
            p2 * maxdelay,
            p3 * maxdelay),
			0.01
        );
    mixed = input + dsig;
    Out.ar(out, mixed);
}).add;



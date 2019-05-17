
~scenes = List(0);


~temp = DAMScene.new();
~temp.prebuild('Reverb | Compressor | Filter');

~tempChain = DAMChain.new();
~tempChain.preBuild('Reverb', [true, true, false, false]);
~temp.setChain(~tempChain, 0);

~tempChain = DAMChain.new();
~tempChain.preBuild('DAMCompressor', [false, false, true, false]);
~temp.setChain(~tempChain, 1);

~tempChain = DAMChain.new();
~tempChain.preBuild('DAMLPF', [false, false, false, true]);
~temp.setChain(~tempChain, 2);


~scenes.add(~temp);

~temp = DAMScene.new();
~temp.prebuild('Looper');
~tempChain = DAMLooper.new();
~temp.setChain(~tempChain, 0);


~scenes.add(~temp);

~temp = DAMScene.new();
~temp.prebuild('Echo | Ladder | Bit Crush' );

~tempChain = DAMChain.new();
~tempChain.preBuild('GreyHoleEcho', [true, true, false, false]);
~temp.setChain(~tempChain, 0);

~tempChain = DAMChain.new();
~tempChain.preBuild('Ladder', [false, false, true, false]);
~temp.setChain(~tempChain, 1);


~tempChain = DAMChain.new();
~tempChain.preBuild('Decimate', [false, false, false, true]);
~temp.setChain(~tempChain, 2);


~scenes.add(~temp);

~temp = DAMScene.new();
~temp.prebuild('Friction | Compressor | Reverb');

~tempChain = DAMChain.new();
~tempChain.preBuild('Friction', [true, false, false, false]);
~temp.setChain(~tempChain, 0);

~tempChain = DAMChain.new();
~tempChain.preBuild('DAMCompressor', [false, true, false, false]);
~temp.setChain(~tempChain, 1);

~tempChain = DAMChain.new();
~tempChain.preBuild('Reverb', [false, false, true, true]);
~temp.setChain(~tempChain, 0);

~scenes.add(~temp);


~temp = DAMScene.new();
~temp.prebuild('Reverb | Looper | Compressor');

~tempChain = DAMChain.new();
~tempChain.preBuild('Reverb', [true, true, false, false]);
~temp.setChain(~tempChain, 0);

~tempChain = DAMLooper.new();
~temp.setChain(~tempChain, 1);


~tempChain = DAMChain.new();
~tempChain.preBuild('Reverb', [false, false, true, true]);
~temp.setChain(~tempChain, 2);

~scenes.add(~temp);



~temp = DAMScene.new();
~temp.prebuild('Flanger');

~tempChain = DAMChain.new();
~tempChain.preBuild('Flanger', [true, false, false, false]);
~temp.setChain(~tempChain, 0);

~tempChain = DAMChain.new();
~tempChain.preBuild('DAMCompressor', [false, true, false, false]);
~temp.setChain(~tempChain, 1);

~scenes.add(~temp);

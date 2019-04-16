
~scenes = List(0);


~temp = DAMScene.new();
~temp.prebuild('Reverb | Distort | Filter');
~tempChain = DAMChain.new();
~tempChain.preBuild('Reverb_Base', [true, true, false, false]);
~temp.setChain(~tempChain, 0);

~tempChain = DAMChain.new();
~tempChain.preBuild('Distort', [false, false, true, false]);
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
~temp.prebuild('Phaser | Chorus | Delay');

~tempChain = DAMChain.new();
~tempChain.preBuild('DAMPhaser', [true, true, false, false]);
~temp.setChain(~tempChain, 0);

~tempChain = DAMChain.new();
~tempChain.preBuild('DAMChorus', [false, false, false, false]);
~temp.setChain(~tempChain, 1);

~tempChain = DAMChain.new();
~tempChain.preBuild('DAMDelay', [false, false, true, true]);
~temp.setChain(~tempChain, 2);


~scenes.add(~temp);


~temp = DAMScene.new();
~temp.prebuild('Compressor');
~tempChain = DAMChain.new();
~tempChain.preBuild('DAMCompressor', [true, true, false, false]);
~temp.setChain(~tempChain, 0);


~scenes.add(~temp);

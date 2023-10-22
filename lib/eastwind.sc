Eastwind {
	const <maxvoices = 10;

	classvar h,a,r;
	classvar itm_count;
	classvar freqlist;
	classvar decylist;
	classvar resfreqlist;
	classvar resdecylist;


	var <synparams;
	var <resonparams;
	// groups
	var fxgroup;
	var loopergroup;
	var syngroup;
	// busses
	var notebus;
	var outerbus;
  var looperbus;
  // buffers
  var oneb;
  var twob;
	// fixed synths
	var loopers;
	var resonsyn;
  var mixsyn;
	// voices
	var allocr;
	var s;

	*initClass {
		StartUp.add {
			var s = Server.default;
			s.waitForBoot {

				h = Harmonics.new(10);
				a = [27,30,34,37,41 ,44,47,51].midicps;
				r = h.ramp(1,1);
				itm_count = a.size * 10;
				freqlist = a.collect( { | n | r * n }).flatten;
				decylist = 1.dup(a.size).collect( { | n |  n * h.decay(0.2); } ).flatten.sort({ | a , b | a > b });

				h = Harmonics.new(3);
				r = h.ramp(1,2);
				resfreqlist = a.collect( { | n | r * n }).flatten;
				resdecylist = 1.dup(a.size).collect( { | n |  n * h.decay(0.2); } ).flatten.sort({ | a , b | a > b });

				"DECAY ".post; decylist.size.postln;

				SynthDef(\ew_tone,{
					var gate = \gate.kr(1) + Impulse.kr(0);
					var mix = Lag.kr(\smix.kr(0),0.5);
					var env = EnvGen.ar(Env.adsr(\atk.kr(2),0.3,1,\rel.kr(0.5)),gate,doneAction:2);
					var vib = Gendy1.kr(1,1,1,1,0.1, 4,mul:0.003,add:1);
					var asig = SinOsc.ar(\freq.kr(120)*vib).distort;
					var bsig =  DPW3Tri.ar(\freq.kr(120)*vib);
					var pw = ((LFTri.ar(0.01) * 0.5) + 1) * 0.3;
					var pwx = ((LFTri.ar(0.02) * 0.5) + 1) * 0.28;
					var csig = Pulse.ar(\freq.kr(120)*vib,pw) +  Pulse.ar((\freq.kr(120) + 0.1)*vib,pwx);
					var sig = SelectX.ar(mix * 3,[asig,bsig,csig]) * \vamp.kr(1) * 0.1;
					var flt = MoogFF.ar(sig,\freq.kr(120) * \fltfreq.kr(1.1),\fltres.kr(0.5));
					Out.ar(\out.ir(0),Pan2.ar(flt * env,0) );
				}).add;


				SynthDef(\ew_looper,{
					var mic, trig, max, ptr, loop, rec, mix;
					var xfade = \xfade.kr(0.02);
					var buf = \loopbuf.ir(0);
					mic = In.ar(\inp.ir(10),2);
					trig = Trig1.ar(\trig.tr(0), ControlDur.ir);
					max = Sweep.ar(trig, SampleRate.ir * \run.kr(0));
					ptr = Phasor.ar(trig, 1, 0, max, 0);
					//loop = AnalogTape.ar(BufRd.ar(2, buf, ptr),bias: 0.7,saturation:0.7,drive:0.6);
					loop = BufRd.ar(2, buf, ptr);
					rec = sum([
						mic * \reclev.kr(0).varlag(xfade,-2),
						loop * \prelev.kr(0).varlag(xfade,-2)
					]);
					rec = rec * \recAmp.kr(1).varlag(xfade,-2);
					BufWr.ar(rec, buf, ptr);
					mix = sum([
						loop * \loopAmp.kr(1).varlag(xfade,-2),
						mic * \micAmp.kr(1).varlag(xfade,-2)
					]);
					mix = mix * \mixAmp.kr(1).varlag(xfade,-2);
					Out.ar(\out.ir(0), mix!2);
				}).add;

				SynthDef(\ew_reson,{
					//var inp = Normalizer.ar(In.ar(\in.kr(10),2),0.4,0.02);
					var inp = In.ar(\in.kr(10),2);
					var sig = DynKlank.ar(
						`[resfreqlist,resdecylist * 0.1 , resdecylist * \decay.kr(1)],
						inp);
					sig = Compander.ar(sig, sig,
						thresh: 0.7,
						slopeBelow: 1,
						slopeAbove: 0.2,
						clampTime:  0.01,
						relaxTime:  0.01
					);
					Out.ar(\out.kr(0),inp + (sig * Lag.kr(\decayamp.kr(1),1) * 0.1) );
				}).add;

				SynthDef(\ew_mixer,{
				 Out.ar(\out.ar(0), ( In.ar(\one.ar(10)) * 0.5) + ( In.ar(\two.ar(10)) * 0.3));
				}).add;
			}
		}
	}

	*new { // when this class is initialized...
		^super.new.init; // ...run the 'init' below.
	}

	init {
		// build a list of our sound-shaping parameters, with default values
		// (see https://doc.sccode.org/Classes/Dictionary.html for more about Dictionaries):
		var s = Server.default;
		notebus = Bus.audio(s,2);
		outerbus = Bus.audio(s,2);
		syngroup = Group.tail(s);
		loopergroup = Group.tail(s);
		fxgroup =  Group.tail(s);

		oneb = Buffer.alloc(s, s.sampleRate * 300.0, 2);
		twob = Buffer.alloc(s, s.sampleRate * 300.0, 2);

		synparams = Dictionary.newFrom([
			\atk, 0.1,
			\rel, 0.4,
			\vamp, 0.2,
			\smix, 0.5,
			\fltfreq, 1.1,
			\fltres, 0.5,
		]);
		resonparams = Dictionary.newFrom([
			\decay, 20,
			\decayamp, 0.4,
		]);
		loopers = Dictionary.newFrom([
			\1,Synth(\ew_looper,[\inp,notebus,\out,looperbus,\loopbuf,oneb],target: loopergroup ),
			\2,Synth(\ew_looper,[\inp,notebus,\out,looperbus,\loopbuf,twob],target: loopergroup ),
		]);
		resonsyn = Synth(\ew_reson,[\in,looperbus,\out,0,\decay,7,\amp,0.5],target:fxgroup);
		//mixsyn = Synth(\ew_mixer,[\one,outerbus,\two,looperbus],target:fxgroup);
		allocr = EWVoiceAllocator.new(maxvoices);
	}

	// note commands

	noteon { arg key;
		var freq = freqlist[key];
		allocr.voiceOn(key,{ Synth(\ew_tone,
			[\out,notebus,\freq, freq] ++ synparams.getPairs,
			target:syngroup)});
	}

	noteoff { arg key;
		allocr.voiceOff(key);
	}

	alloff {
		(0..80).do({ arg n; this.noteoff(n); });
	}

	// looper commands
	// ln is looper - \1 or \2
	// NO ERROR CHECKING
	// if you aren't directly wiring the UI for this
	// then maybe add some precautions

	// record for the first time
	record {
	  | ln |
	  loopers[ln].set(\trig, 1, \run, 1, \reclev, 1, \prelev, 1, \xfade, 0.02);
	}

	// put in overdub mode
	// if 'recording' is ON then
	// will actually be overdubbing
	// otherwise just play
	overdub {
	 | ln , fadev |
	 loopers[ln].set(\run, 0, \reclev, 1, \prelev, fadev.dbamp );
	}

	// clear loop length and
	// wait for new recording
	// doesnt' clear buffer but
	// that shouldn't matter
	reset {
	  | ln |
	  if ( ln == \1 ) { oneb.zero } { twob.zero };
		loopers[ln].set(\trig, 1, \run, 0, \reclev, 0, \prelev, 0, \xfade, 0.02);
	}

	// set recording status
	recording {
	  | ln, state |
	  loopers[ln].set( \reclev, state )
	}

	fadelevel {
	  | ln, fadev |
	  loopers[ln].set( \prelev,fadev.dbamp);
	}


	// NOTE - even though we split the params into sections
	// they are all expected to be unique so this works
	setParam { arg paramKey, paramValue;
		if (synparams.includesKey(paramKey) ) {
			synparams[paramKey] = paramValue;
			syngroup.set(paramKey,paramValue);
			"set ".post; paramKey.post; " to ".post; paramValue.postln;
		};
		if (resonparams.includesKey(paramKey) ) {
			resonparams[paramKey] = paramValue;
			resonsyn.set(paramKey,paramValue);
		};
	}

	allparams {
		^(synparams ++ resonparams);
	}

	item_count {
		^itm_count;
	}

	free_all {
		this.alloff;
		oneb.free;
		twob.free;
		fxgroup.free;
		loopergroup.free;
		syngroup.free;

	}




}
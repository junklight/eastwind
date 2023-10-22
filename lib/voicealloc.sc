
EWVoice {

	var <syn = nil;
	var <>key = nil;

	syn_ { | s |
		syn = s;
		NodeWatcher.register(s,true);
	}

	// after checkfree has run
	// we can be sure the synth has freed
	// and we can reuse the voice
	checkFree {
		if (syn != nil) {
			if (syn.isPlaying ) {
				syn.release(-1);
				syn.free;
				syn = nil;
			} {
				syn = nil;
			};
		}
	}

	printOn { | stream |
        stream << "EWVoice( " << syn << ", " << key << " )";
    }
}

EWVoiceAllocator {

	var voices;
	var freelist;
	var uselist;
	var keydict;
	var voices;

	*new {
		| n=5 |
		^super.new.init(n);
	}

	init {
		| n |
		// I'd quite like this to be dynamic
		// but I'll come back to that one day
		voices = n;
		// list of internal ids for voices
		// not to be confused with key which is
		// the external id for a voice
		freelist = Queue.newUsing(n.collect({ | n | n.asSymbol }));
		uselist = Queue.new;
		voices = Dictionary.new;
		keydict = Dictionary.new;
		freelist.do({
			| voiceid |
			voices[voiceid] = EWVoice.new;
		})
	}

	voiceOn {
		| key , fn |
		var voiceid;
		// if there is a free voice
		// get the oldest one
		// make sure it's free
		// and use it
		// failing that get the oldest
		// in use voice and reuse it
		if (freelist.size > 0 ) {
			voiceid = freelist.dequeue;

		} {
			voiceid = uselist.dequeue;
		};
		uselist.enqueue(voiceid);
		voices[voiceid].checkFree;
		voices[voiceid].syn = fn.value;
		voices[voiceid].key = key;
		keydict[key] = voiceid;
	}

	voiceOff {
		| key |
		var voiceid = keydict[key];
		if (voiceid != nil ) {
			var voice = voices[voiceid];
			if (voice != nil ) {
				if (voice.key == key ) {
					voice.syn.set(\gate,0);
					voice.key = nil;
				}
			}
		}
	}


}


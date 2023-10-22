Engine_Eastwind : CroneEngine {
	var kernel;

	*new { arg context, doneCallback;
		^super.new(context, doneCallback);
	}

	alloc {

		kernel = Eastwind.new(Crone.server);

		this.addCommand(\noteon, "i", { arg msg;
			var key = msg[1].asInteger;
			kernel.noteon(key);
		});

		this.addCommand(\noteoff, "i", { arg msg;
			var key = msg[1].asInteger;
			kernel.noteoff(key);
		});

		kernel.allparams.keysValuesDo({ arg paramKey;
			this.addCommand(paramKey, "f", {arg msg;
				kernel.setParam(paramKey.asSymbol,msg[1].asFloat);
			});
		});

		// NEW: add a command to free all the voices
		this.addCommand(\free_all_notes, "", {
			kernel.alloff;
		});
		
		this.addCommand(\dnodes,"", {
		  this.dnodes;
		});
		
		// looper 
		
		this.addCommand(\record, "i" , { 
		  arg msg;
		  var loopid = msg[1].asSymbol;
		  kernel.record(loopid);
	  });

		this.addCommand(\overdub, "if" , { 
		  arg msg;
		  var loopid = msg[1].asSymbol;
		  var fadev = msg[2].asFloat;
		  kernel.overdub(loopid,fadev);
	  });
	  
	  this.addCommand(\reset, "i" , { 
		  arg msg;
		  var loopid = msg[1].asSymbol;
		  kernel.reset(loopid);
	  });
	  
	  this.addCommand(\recording, "ii" , { 
		  arg msg;
		  var loopid = msg[1].asSymbol;
		  var state = msg[2].asInteger;
		  kernel.recording(loopid,state);
	  });
	  
	  this.addCommand(\fadelevel, "if" , { 
		  arg msg;
		  var loopid = msg[1].asSymbol;
		  var fadev = msg[2].asFloat;
		  kernel.fadelevel(loopid,fadev);
	  });
	}
	  
	free {
		kernel.free_all;

	}
	
	// useful debug 
	dnodes {
	  Crone.server.queryAllNodes.postln;
	}

} 
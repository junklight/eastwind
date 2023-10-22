
local eastwind_params = {}
local cs = require 'controlspec'
local Formatters = require 'formatters'

-- helper function to round and format parameter value text:
function round_form(param,quant,form)
  return(util.round(param,quant)..form)
end


specs = {} 

specs.TIME = cs.def{
                min=0.1,
                max=10,
                warp='exp',
                step=0.1,
                default=1,
                wrap=false,
              }
specs.ONE = cs.def{
                min=0.0,
                max=1.0,
                warp='lin',
                step=0.05,
                default=0.3,
                wrap=false,
              }
specs.FLT = cs.def{
                min=0.1,
                max=2.0,
                warp='lin',
                step=0.05,
                default=1.0,
                wrap=false,
              }       
specs.FLTRES = cs.def{
                min=0.1,
                max=4.0,
                warp='lin',
                step=0.05,
                default=1.0,
                wrap=false,
              }                


function eastwind_params.add_params()
  params:add_separator("Eastwind")
  params:add_group("sound","sound",6)
-- attack
  params:add{
          type="control",
          id="attack",
          name="attack",
          controlspec=specs.TIME,
          action=function(x) engine.atk(x) end 
        }
-- release 
  params:add{
          type="control",
          id="release",
          name="release",
          controlspec=specs.TIME,
          action=function(x) engine.rel(x) end 
        }
  
-- smix 
  params:add{
          type="control",
          id="smix",
          name="sound",
          controlspec=specs.ONE,
          action=function(x) engine.smix(x) end 
        }
  
-- amp 
  params:add{
          type="control",
          id="voiceamp",
          name="voiceamp",
          controlspec=specs.ONE,
          action=function(x) engine.vamp(x) end 
        }
        
  params:add{
          type="control",
          id="fltfreq",
          name="filter relative freq",
          controlspec=specs.FLT,
          action=function(x) engine.fltfreq(x) end 
        }   
      
  params:add{
          type="control",
          id="fltres",
          name="filter res",
          controlspec=specs.FLTRES,
          action=function(x) engine.fltres(x) end 
        }    
        
  params:add_group("resontator",2)
-- resonantor 
-- decay 
  params:add_number(
          'decay',
          'decay',
          0.05,
          20,
          3,
          function(param) return (round_form(param:get(),0.01," s")) end
        )
  params:set_action('decay',function(x) engine.decay(x) end )                        
  -- amp 

  params:add{
          type="control",
          id="amp",
          name="amp",
          controlspec=specs.ONE,
          action=function(x) engine.decayamp(x) end 
        }


  
                    
-- no looper controls for the moment 
  params:bang()
end


return eastwind_params